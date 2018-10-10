# 这是什么
这是一个本地评测的脚本(`judge.kts`)  
当然, 看到 `.kts` 这个~~高级的~~文件后缀就知道需要 [`kotlin`](https://kotlinlang.org) 编译器了

# 如何使用
首先, 创建一个配置文件 `config.sh`  
需要包含如下内容  
```
projectRoot="工程的根目录"
buildDir="工程的输出目录"
judgeDir="评测脚本所在目录"
projectName="工程名称"
genTarget="生成目标路径"
```  
随后, 运行以下命令  
```Shell
./build <problemid>
```
更多信息请运行 `./build -help`  