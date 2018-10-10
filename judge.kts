#!/usr/bin/kotlinc -script

import java.io.*
import java.nio.file.Paths
import java.util.Scanner

val prefix = ".."
val runtime : Runtime = Runtime.getRuntime()

class JudgeException(val problem : Int, msg : String) : Exception("Problem $problem : $msg")

fun process(id : Int, input : String, output : String) : Long {
	val start = System.currentTimeMillis()

	val proc : Process = runtime.exec("$prefix/build/LuoGu")
	val stdin : OutputStream = proc.outputStream
	val stdout : InputStream = proc.inputStream

	stdin.use { out ->
		out.write(input.toByteArray())
		out.flush()
	}

	proc.waitFor()

	val outS = Scanner(stdout)
	val ansS = Scanner(output)

	while (outS.hasNext()/* && ansS.hasNext()*/) {
		val out = outS.next()
		val ans = ansS.next()

		if (out != ans) {
			throw JudgeException(id, """expect "$ans", but got "$out"""")
		}
	}

	val end = System.currentTimeMillis()
	if (ansS.hasNext()) throw JudgeException(id, """answer length is too short""")

	return end - start
}

val pid = args.getOrNull(0)?.toInt() ?: throw RuntimeException("Please input problem id")
val testDir = File("${prefix}/test/$pid/")

if (! testDir.exists()) throw JudgeException(pid, "No found test data")

testDir.listFiles()?.forEach {
	if (it.isDirectory) {
		println("Testing ${it.name}...")

		val input = Paths.get(it.path, "in.txt").toAbsolutePath().toFile().readText()
		val output = Paths.get(it.path, "out.txt").toAbsolutePath().toFile().readText()

		println("use ${process(pid, input, output)} ms")
		println("Accepted!!")
	}
}

println("All Accepted!!")