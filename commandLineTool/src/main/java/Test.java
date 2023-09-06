import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
A script that automates the process of generating benchmark for the Sqrt class.
It first calls EvoSuite to generate unit test from the class' source code.
Then it feeds the output (EviSuite tests) into ju2jmh to generate benchmarks.
 **/
public class Test {
    private static boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public static void main(String[] args) throws Exception {

        // 1: build project
        String projectLocation = "/Users/yiyaochen/Desktop/master-thesis/newtool/junit-to-jmh-experiment/slowdown-detection";
        File location = new File(projectLocation);
        runCommand(location, "./gradlew build");

        // 2: generate unit tests for the specified class Sqrt using EvoSuite
        String command2 = "java -jar ../../evosuite-1.2.0.jar -class se.chalmers.ju2jmh.experiments.workloads.Sqrt -projectCP main/build/classes/java/main";
        runCommand(location, command2);

        // 3: move generated EvoSuite tests from 'evosuite-tests' to 'src/test/java' so that they can be run with './gradlew test'
        try (Stream<Path> stream = Files.list(Paths.get("/Users/yiyaochen/Desktop/master-thesis/newtool/junit-to-jmh-experiment/slowdown-detection/evosuite-tests/se/chalmers/ju2jmh/experiments/workloads"))) {
            List<Path> paths = stream.filter(path -> path.toString().endsWith(".java")).collect(Collectors.toList());

            for (Path source : paths) {
                System.out.println("src: "+source.toString());
                Path destination = Paths.get("/Users/yiyaochen/Desktop/master-thesis/newtool/junit-to-jmh-experiment/slowdown-detection/main/src/test/java/se/chalmers/ju2jmh/experiments/workloads" + File.separator + source.getFileName());
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // change build.gradle.kts to include EvoSuite jar dependencies
        //  4: run tests
        runCommand(location, "./gradlew test");


        // 5: run a Python script to get class name list and store them in 'test-classes.txt'
        location = new File("/Users/yiyaochen/Desktop/master-thesis/newtool/junit-to-jmh-experiment");
        String command5 = "python3 scripts/gradle/list_tests.py --classes-only --plaintext-output slowdown-detection/main/build/test-results/test/ test-classes.txt";
        runCommand(location, command5);

        // 6: generate benchmark using ju2jmh
        location = new File("/Users/yiyaochen/Desktop/master-thesis/newtool/junit-to-jmh");
        runCommand(location, "chmod +x gradlew");
        String testClasses = "/Users/yiyaochen/Desktop/master-thesis/newtool/junit-to-jmh-experiment/slowdown-detection/main/src/test/java/";
        String compiledTestClasses = "/Users/yiyaochen/Desktop/master-thesis/newtool/junit-to-jmh-experiment/slowdown-detection/main/build/classes/java/test/";
        String destination = "/Users/yiyaochen/Desktop/master-thesis/newtool/commandLineTool/src/main/java/";
        String classNames = "--class-names-file=/Users/yiyaochen/Desktop/master-thesis/newtool/junit-to-jmh-experiment/test-classes.txt";
        String command6 = "./gradlew converter:run --args=\"" + testClasses + " " + compiledTestClasses + " " + destination + " " + classNames + "\"";
        runCommand(location, command6);

    }

    /**
    A utility function used to run shell commands within a specified directory
     **/
    public static void runCommand(File whereToRun, String command) throws Exception {
        System.out.println("Running in: " + whereToRun);
        System.out.println("Command: " + command);

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(whereToRun);

        if(isWindows) {
            builder.command("cmd.exe", "/c", command);
        }else {
            builder.command("sh", "-c", command);
        }

        Process process = builder.start();

        OutputStream outputStream = process.getOutputStream();
        InputStream inputStream = process.getInputStream();
        InputStream errorStream = process.getErrorStream();

        printStream(inputStream);
        printStream(errorStream);

        boolean isFinished = process.waitFor(30, TimeUnit.SECONDS);
        outputStream.flush();
        outputStream.close();

        if(!isFinished) {
            process.destroyForcibly();
        }
    }

    private static void printStream(InputStream inputStream) throws IOException {
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
