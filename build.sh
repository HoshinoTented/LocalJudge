#!/usr/bin/env bash

justRun=0
language="CXX"
solution=""
configName="config"
compare="true"

# read from config.sh
projectRoot=""
buildDir=""
judgeDir=""
projectName=""
genTarget=""
sourceDir=""

# 读取配置文件(key=value)
function loadConfig() {
#    while read line; do
#        eval "${line}"
#    done < ${configFile}

    source "${configName}.sh"

    if [[ $1 = "" ]]
    then
        source "${configName}$1.sh"
    fi
}

function wrongOption() {
    echo "Use \"--help\" to get more infomation"
}

function helpDoc() {
    echo "usage: build <solution> [-r] [-k | -c]
    <solution>: 题解名称, 如 P1000
    [-r]: 仅运行题解
    [-k | -c | -h]: 题解的语言, 默认为 \"CXX\"
            -k 为 Kotlin
            -c 为 CXX
            -h 为 Haskell
    例如, 仅运行(不进行评测) P1000 的 Haskell 题解:
    build -h -r P1000"
}

# 返回 0 表示无错误
# 返回 255 表示结束脚本运行
function loadOptions() {
    # 读入运行选项
    for param in "$@"
    do
        if [[ "${param:0:1}" = "-" ]]
        then
            local option="${param:1}"
            case "${option}" in
            "r")
               justRun=1 ;;

            "k"|"kt"|"kotlin")
                language="Kotlin" ;;
            "c"|"cpp")
                language="CXX" ;;
            "h"|"hs"|"haskell")
                language="Haskell" ;;
	    "withoutCompare")
		compare="false" ;;
            "help"|"-help")
                helpDoc
                exit ;;
            *)
                echo "Unknown command '${param}'"
                wrongOption
                exit ;;
            esac
        else
            solution="${param}"
        fi
    done

    return 0
}

function preRunKotlin() {
    cd "${buildDir}"
    echo "Copying source file"
    cp "${sourceDir}/${solution}.kt" "."

    echo "
fun main() = ${solution}.main()" >> "${solution}.kt"

    echo "Compiling ${solution}.kt"
    kotlinc "${solution}.kt"

    local content="#!/usr/bin/env bash
cd ${buildDir}
kotlin ${solution}Kt"

    echo "${content}" > "${genTarget}"
}

function preRunHaskell() {
    echo -n "-- " > "${genTarget}.hs"
    cat "${sourceDir}/${solution}.hs" >> "${genTarget}.hs"

    echo "Compiling ${solution}.hs"
    ghc "${genTarget}.hs"

    cd "${sourceDir}"
}

function preRunCXX() {
    local header="
    #include \"src/${solution}.hpp\"
    "

    echo "${header}" > "${projectRoot}/main.cpp"

    cd "${buildDir}"
    make
}

function runSolution() {
    echo "See https://www.luogu.org/problemnew/show/${solution} for more information"

    chmod +x "${genTarget}"

    if [[ "${justRun}" == 1 ]]
    then
        ${genTarget}
    else
        rm ${projectRoot}/output/*
        echo "Running judge script"
        ${judgeDir}/src/judge.kts "problem=${solution}" "prefix=${projectRoot}" "name=${projectName}" "compare=${compare}"
    fi
}

loadConfig
loadOptions "$@"

if [[ "${solution}" = "" ]]
then
    echo "Please input solution id"
    wrongOption
else
    # 根据运行选项生成不同的运行程序
    preRun${language}
    cd "${judgeDir}"

    # 运行生成后的程序
    runSolution
fi
