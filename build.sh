#!/usr/bin/env bash

justRun=0
language="CXX"
problemId=""
projectRoot="/home/hoshino/Documents/Projects/CLion/LuoGu"
buildDir="${projectRoot}/build"
judgeDir="${projectRoot}/judge"
projectName="LuoGu"
genTarget="${buildDir}/${projectName}"

# 返回 0 表示无错误
# 返回 1 表示未知选项
function loadOptions() {
    # 读入运行选项
    for param in "$@"
    do
        if [ "${param:0:1}" = "-" ]
        then
            option=${param:1}
            if [ "${option}" = "r" ]
            then
                justRun=1
            elif [ "${option}" = "k" ]
            then
                language="Kotlin"
            elif [ "${option}" = "c" ]
            then
                language="CXX"
            else
                echo "Unknown command -${option}"
                return 1
            fi
        else
            problemId=${param}
        fi
    done

    return 0
}

function preRunKotlin() {
    echo "Compiling ${problemId}.kt"
    kotlinc -d "${buildDir}/kotlin/" ../src/${problemId}.kt

    content="
    #!/usr/bin/env bash\n
    cd ${buildDir}/kotlin;
    kotlin _${problemId}Kt
    "

    echo -e ${content} > ${genTarget}
}

function preRunCXX() {
    header="
    #include \"src/${problemId}.hpp\"
    "

    echo ${header} > ../main.cpp

    cd ${buildDir}
    make
    cd ${judgeDir}
}

function runSolution() {
    echo "See https://www.luogu.org/problemnew/show/P${problemId} for more information"
    if [ "${justRun}" == 1 ]
    then
        ${genTarget}
    else
        ./judge.kts ${problemId}
    fi
}

loadOptions "$@"

if [ $? == 1 ]
then
    exit
elif [ "${problemId}" = "" ]
then
    echo "Please input solution id"
else
    # 根据运行选项生成不同的运行程序
    preRun${language}

    # 运行生成后的程序
    runSolution
fi