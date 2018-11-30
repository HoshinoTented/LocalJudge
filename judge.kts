#!/usr/bin/kotlinc -script

import java.io.*
import java.nio.file.Paths
import java.util.Scanner

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

		(0 until out.length).forEach {
			val outC = out[it]
			val ansC = ans[it]
			val isPrint : (Char) -> String = { c ->
				if (' '.toInt() < c.toInt()) c.toString() else "ASCII(${c.toInt()})"
			}

			if (outC != ansC) {
				throw JudgeException(id, "line $line column ${it + 1}, expect ${isPrint(ansC)} but got ${isPrint(outC)}")
			}
		}

		++ line
	}

	val end = System.currentTimeMillis()
	if (ansS.hasNext()) throw JudgeException(id, """answer length is too short""")

	return end - start
}

fun loadParams() : Map<String, String> {
	return args.map {
		it.split('=').run {
			get(0) to get(1)
		}
	}.toMap()
}

val config : Map<String, String> = loadParams()

val pid = config["problem"] ?: throw IllegalArgumentException("Please input problem id, use param '-p <problem id>'")
val prefix = config["prefix"] ?: throw IllegalArgumentException("Please input path's prefix, use param '-P <prefix>'")
val projectName = config["name"] ?: throw IllegalArgumentException("Please input project name, use param '-n <name>'")
val inFileName = config["in"] ?: "in.txt"
val outFileName = config["out"] ?: "out.txt"
val testDir = File("$prefix/test/$pid/")
val executable = "$prefix/build/$projectName"
val runtime : Runtime = Runtime.getRuntime()


if (! testDir.exists()) throw JudgeException(pid, "No found test data")

testDir.listFiles()?.forEach {
	if (it.isDirectory) {
		println("Testing ${it.name}...")

		val inFile = Paths.get(it.path, inFileName).toFile().takeIf(File::exists) ?: Paths.get(it.path, "testdata.in").toFile().takeIf(File::exists) ?: throw JudgeException(pid, "Not found input test data")
		val outFile = Paths.get(it.path, outFileName).toFile().takeIf(File::exists) ?: Paths.get(it.path, "testdata.out").toFile().takeIf(File::exists) ?: throw JudgeException(pid, "Not found output test data")

		val input = inFile.readText()
		val output = outFile.readText()

		println("use ${process(pid, input, output)} ms")
		println("Accepted!!")
	}
}

println("All Accepted!!")