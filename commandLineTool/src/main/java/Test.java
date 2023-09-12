import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
A script that automates the process of generating benchmark for the Sqrt class.
It first calls EvoSuite to generate unit test from the class' source code.
Then it feeds the output (EviSuite tests) into ju2jmh to generate benchmarks.
 **/
public class Test implements Runnable {
    private static boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    @CommandLine.Option(names = {"-c", "--config"}, description = "Path to the configuration file (e.g., benchmark-config.properties)")
    private String configFile;

    // Other command-line options and arguments can be defined here using @Option and @Parameters annotations

    public static void main(String[] args) {
        new CommandLine(new Test()).execute(args);
    }

    @Override
    public void run() {
        try {
            Configuration config = loadConfiguration(configFile);

            // 1: build project
            String projectLocation = config.getProjectLocation();
            File location1 = new File(projectLocation);
            runCommand(location1, "chmod +x gradlew");
            String command1 = "./gradlew build";
            runCommand(location1, command1);

            // 2: generate unit tests for the specified class using EvoSuite
            String evoSuiteJarPath = config.getEvoSuiteJarPath();
            String classToTest = config.getClassToTest();

            String projectClassPath = config.getProjectClassPath();
            String command2 = "java -jar " + evoSuiteJarPath + " -class " + classToTest + " -projectCP " +  projectClassPath;
            runCommand(location1, command2);

            // 3: move generated EvoSuite tests from 'evosuite-tests' to 'src/test/java' so that they can be run with './gradlew test'
            String evosuiteTestLocation = config.getEvosuiteTestLocation();
            String defaultTestLocation = config.getDefaultTestLocation();
            try (Stream<Path> stream = Files.list(Paths.get(evosuiteTestLocation))) {
                List<Path> paths = stream.filter(path -> path.toString().endsWith(".java")).collect(Collectors.toList());

                for (Path source : paths) {
                    Path destination = Paths.get(defaultTestLocation + File.separator + source.getFileName());
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // change build.gradle.kts to include EvoSuite jar dependencies
            //  4: run tests
            String command3 = "./gradlew test";
            runCommand(location1, command3);

            // 5: run a Python script to get class name list and store them in 'test-classes.txt'
            String classNamesGeneratorPath = config.getClassNamesGeneratorPath();
            String xmlReportsPath = config.getXmlReportsPath();
            String classNamesDestination = config.getClassNamesDestination();
            String command4 = "python3 " + classNamesGeneratorPath + " --classes-only --plaintext-output " + xmlReportsPath + " " + classNamesDestination;
            runCommand(location1, command4);


            // 6: generate benchmark using ju2jmh
            String ju2jmhPath = config.getJu2jmhPath();
            File location2 = new File(ju2jmhPath);
            runCommand(location2, "chmod +x gradlew");
            String testClasses = config.getTestPath();
            String compiledTestClasses = config.getCompiledTestLocation();
            String destination = config.getBenchmarkDestination();
            String classNames = config.getClassNamesPath();
            String command6 = "./gradlew converter:run --args=\"" + testClasses + " " + compiledTestClasses + " " + destination + " --class-names-file=" + classNames + "\"";
            runCommand(location2, command6);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Configuration loadConfiguration(String configFile) throws IOException {
        Configuration config = new Configuration();

        try (InputStream input = new FileInputStream(configFile)) {
            config.load(input);
        }

        return config;
    }

    // Define a Configuration class to represent your properties
    private static class Configuration {
        // Define fields that correspond to properties in the .properties file
        // Use @Option or @Parameters annotations if these can be overridden via command-line arguments
        private String projectLocation;
        private String evoSuiteJarPath;
        private String classToTest;
        private String projectClassPath;
        private String evosuiteTestLocation;
        private String defaultTestLocation;
        private String classNamesGeneratorPath;
        private String xmlReportsPath;
        private String classNamesDestination;
        private String compiledTestLocation;
        private String testPath;
        private String ju2jmhPath;
        private String benchmarkDestination;
        private String classNamesPath;

        // Add getters for these fields
        public String getProjectLocation() {
            return projectLocation;
        }

        public String getEvoSuiteJarPath() {
            return evoSuiteJarPath;
        }

        public String getClassToTest() {
            return classToTest;
        }

        public String getProjectClassPath() {
            return projectClassPath;
        }

        public String getEvosuiteTestLocation() {
            return evosuiteTestLocation;
        }

        public String getDefaultTestLocation() {
            return defaultTestLocation;
        }

        public String getClassNamesGeneratorPath() {
            return classNamesGeneratorPath;
        }

        public String getXmlReportsPath() {
            return xmlReportsPath;
        }

        public String getClassNamesDestination() {
            return classNamesDestination;
        }

        public String getCompiledTestLocation() {
            return compiledTestLocation;
        }

        public String getTestPath() {
            return testPath;
        }

        public String getJu2jmhPath() {
            return ju2jmhPath;
        }

        public String getBenchmarkDestination() {
            return benchmarkDestination;
        }

        public String getClassNamesPath() {
            return classNamesPath;
        }

        // Other getters for other configuration properties

        public void load(InputStream input) throws IOException {
            Properties properties = new Properties();
            properties.load(input);

            // Populate the fields from the properties
            projectLocation = properties.getProperty("project.location");
            evoSuiteJarPath = properties.getProperty("evosuite.jar.path");
            classToTest = properties.getProperty("class.to.test");
            projectClassPath = properties.getProperty("project.class.path");
            evosuiteTestLocation = properties.getProperty("evosuite.test.location");
            defaultTestLocation = properties.getProperty("default.test.location");
            classNamesGeneratorPath = properties.getProperty("class.names.generator.path");
            xmlReportsPath = properties.getProperty("xml.reports.path");
            classNamesDestination = properties.getProperty("class.names.destination");
            compiledTestLocation = properties.getProperty("compiled.test.location");
            testPath = properties.getProperty("test.path");
            ju2jmhPath = properties.getProperty("ju2jmh.path");
            benchmarkDestination = properties.getProperty("benchmark.destination");
            classNamesPath = properties.getProperty("class.names.path");


        }
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
