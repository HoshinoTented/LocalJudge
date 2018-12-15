#!/usr/bin/kotlinc -script

import java.io.*
import java.nio.file.Paths
import java.util.Scanner

class JudgeException(val problem : String, msg : String) : Exception("Problem $problem : $msg")

fun process(input : InputStream, output : OutputStream) : Long {
	val start = System.currentTimeMillis()

	val proc : Process = runtime.exec(executable)
	val stdin : OutputStream = proc.outputStream
	val stdout : InputStream = proc.inputStream

	val outS = Scanner(stdout)

	stdin.use { out ->
		input.copyTo(out)
		out.flush()
	}

	proc.waitFor()

	outS.use {
		while (it.hasNext()) {
			output.write(it.nextLine().toByteArray())
		}
	}

	val end = System.currentTimeMillis()

	return end - start
}

fun compare(id : String, out : InputStream, ans : InputStream) {
	val outS = Scanner(out)
	val ansS = Scanner(ans)

	var line = 1

	while (outS.hasNext()) {
		val outLine = outS.nextLine().trimEnd()
		val ansLine = ansS.nextLine().trimEnd()

		(0 until outLine.length).forEach {
			val outC = outLine[it]
			val ansC = ansLine[it]
			val isPrint : (Char) -> String = { c ->
				if (' '.toInt() < c.toInt()) c.toString() else "ASCII(${c.toInt()})"
			}

			if (outC != ansC) {
				throw JudgeException(id, "line $line column ${it + 1}, expect ${isPrint(ansC)} but got ${isPrint(outC)}")
			}
		}

		++ line
	}

	if (ansS.hasNext()) throw JudgeException(id, """answer length is too short""")
}

fun loadParams(args : Array<String>) : Map<String, String> {
	return args.map {
		it.split('=').run {
			get(0) to get(1)
		}
	}.toMap()
}

lateinit var config : Map<String, String>

val pid by lazy { config["problem"] ?: throw IllegalArgumentException("Please input problem id, use param 'problem=<problem id>'") }
val prefix by lazy { config["prefix"] ?: throw IllegalArgumentException("Please input path's prefix, use param 'prefix=<prefix>'") }
val projectName by lazy { config["name"] ?: throw IllegalArgumentException("Please input project name, use param 'name=<name>'") }
val outputDir by lazy { config["outDir"] ?: "$prefix/output" }
val withoutCompare by lazy { config["compare"] == "false" }
val inFileName by lazy { config["in"] ?: "in.txt" }
val outFileName by lazy { config["out"] ?: "out.txt" }
val testDir by lazy { File("$prefix/test/$pid/") }
val executable by lazy { "$prefix/build/$projectName" }
val runtime : Runtime = Runtime.getRuntime().apply {
	exec("echo init")
}

config = loadParams(args)

if (! testDir.exists()) throw JudgeException(pid, "No found test data")

testDir.listFiles()?.forEach {
	if (it.isDirectory) {
		println("Testing ${it.name}...")

		val inFile = Paths.get(it.path, inFileName).toFile().takeIf(File::exists) ?: Paths.get(it.path, "testdata.in").toFile().takeIf(File::exists) ?: throw JudgeException(pid, "Not found input test data")
		val output = File("$outputDir/${it.name}.out")
		val usedTime = process(inFile.inputStream(), output.outputStream())

		println("use $usedTime ms")

		if (withoutCompare.not()) {
			val outFile = Paths.get(it.path, outFileName).toFile().takeIf(File::exists) ?: Paths.get(it.path, "testdata.out").toFile().takeIf(File::exists) ?: throw JudgeException(pid, "Not found output test data")
			
			println("Comparing")
			compare(pid, output.inputStream(), outFile.inputStream())
			println("Accepted!!")
		}
	}
}

(if (withoutCompare) "Finished" else "All Accepted!!").run(::println)
