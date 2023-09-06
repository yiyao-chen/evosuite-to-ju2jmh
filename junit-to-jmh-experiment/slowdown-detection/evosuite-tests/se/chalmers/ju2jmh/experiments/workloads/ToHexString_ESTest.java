/*
 * This file was automatically generated by EvoSuite
 * Tue Sep 05 15:01:04 GMT 2023
 */

package se.chalmers.ju2jmh.experiments.workloads;

import org.junit.Test;
import static org.junit.Assert.*;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;
import se.chalmers.ju2jmh.experiments.workloads.ToHexString;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true) 
public class ToHexString_ESTest extends ToHexString_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      String string0 = ToHexString.runWorkload(61.707995);
      assertEquals("0x1.eda9f94855da2p5", string0);
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      ToHexString toHexString0 = new ToHexString();
      assertEquals((-1.2345E-66), ToHexString.INPUT, 0.01);
  }
}
