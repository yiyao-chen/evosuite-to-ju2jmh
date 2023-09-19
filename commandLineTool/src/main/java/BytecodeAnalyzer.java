import org.objectweb.asm.*;
import java.io.*;

public class BytecodeAnalyzer {
    public static void main(String[] args) throws Exception {
        //String className = "se.chalmers.ju2jmh.experiments.workloads.Sqrt"; // Name of the class to analyze

        // Specify the full path to the class files or JAR file of the other project
        String classpath = "/Users/yiyaochen/Desktop/master-thesis/newtool-rxJava/RxJava/build/classes/java/jmh"; // Replace with the correct path

        // Specify the fully qualified class name you want to analyze
        String className = "io.reactivex.rxjava3.core.BlockingPerf";

        // Combine the classpath and class name to create the full class file path
        String classFilePath = classpath + File.separator + className.replace('.', File.separatorChar) + ".class";

        ClassReader cr = new ClassReader(new FileInputStream(classFilePath));
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new MethodCallVisitor(Opcodes.ASM9, cw);
        cr.accept(cv, 0);
    }
}

class MethodCallVisitor extends ClassVisitor {
    public MethodCallVisitor(int api) {
        super(api);
    }

    public MethodCallVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        return new CustomMethodVisitor(api, mv, name);
    }
}

class CustomMethodVisitor extends MethodVisitor {
    private final String methodName;

    public CustomMethodVisitor(int api, MethodVisitor mv, String methodName) {
        super(api, mv);
        this.methodName = methodName;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        System.out.println("Method " + methodName + " calls: " + owner + "." + name);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }
}
