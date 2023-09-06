/*
 * This file was automatically generated by EvoSuite
 * Tue Sep 05 14:59:57 GMT 2023
 */
package se.chalmers.ju2jmh.experiments.workloads;

import org.junit.Test;
import static org.junit.Assert.*;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;
import se.chalmers.ju2jmh.experiments.workloads.Sqrt;

@RunWith(EvoRunner.class)
@EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true)
public class Sqrt_ESTest extends Sqrt_ESTest_scaffolding {

    @Test(timeout = 4000)
    public void test0() throws Throwable {
        double double0 = Sqrt.runWorkload(0.0);
        assertEquals(0.0, double0, 0.01);
    }

    @Test(timeout = 4000)
    public void test1() throws Throwable {
        Sqrt sqrt0 = new Sqrt();
        assertEquals(25.0, Sqrt.INPUT, 0.01);
    }

    @Test(timeout = 4000)
    public void test2() throws Throwable {
        double double0 = Sqrt.runWorkload(1309.0);
        assertEquals(36.180105030251084, double0, 0.01);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.experiments.workloads.Sqrt_ESTest_scaffolding._Benchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test0() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test0, this.description("test0"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test1, this.description("test1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test2, this.description("test2"));
        }

        private Sqrt_ESTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new Sqrt_ESTest();
        }

        @java.lang.Override
        public Sqrt_ESTest implementation() {
            return this.implementation;
        }
    }
}
