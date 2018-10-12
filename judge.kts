#!/usr/bin/kotlinc -script

import java.io.*
import java.nio.file.Paths
import java.util.Scanner

val pid = args.getOrNull(0) ?: throw RuntimeException("Please input problem id")
val prefix = args.getOrNull(1) ?: "."
val projectName = args.getOrNull(2) ?: throw RuntimeException("Unknown Project Name")
val testDir = File("$prefix/test/$pid/")
val executable = "$prefix/build/$projectName"
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

	var line = 1

	while (outS.hasNext()) {
		val out = outS.nextLine().trimEnd()
		val ans = ansS.nextLine().trimEnd()

		(1 until out.length).forEach {
			val outC = out[it]
			val ansC = ans[it]
			val isPrint : (Char) -> String = { c ->
				if (' '.toInt() < c.toInt()) c.toString() else "ASCII(${c.toInt()})"
			}

			if (outC != ansC) {
				throw JudgeException(id, "line $line, expect ${isPrint(ansC)} but got ${isPrint(outC)}")
			}
		}

		++ line
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