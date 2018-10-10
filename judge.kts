#!/usr/bin/kotlinc -script

import java.io.*
import java.nio.file.Paths
import java.util.Scanner

val pid = args.getOrNull(0) ?: throw RuntimeException("Please input problem id")
val prefix = args.getOrNull(1) ?: "."
val testDir = File("${prefix}/test/$pid/")
val executable = "$prefix/build/LuoGu"
val runtime : Runtime = Runtime.getRuntime()

class JudgeException(val problem : String, msg : String) : Exception("Problem $problem : $msg")

fun process(id : String, input : String, output : String) : Long {
	val start = System.currentTimeMillis()

	val proc : Process = runtime.exec(executable)
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