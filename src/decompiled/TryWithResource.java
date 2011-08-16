package decompiled;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class TryWithResource {
    public static void main(String[] pArgv) throws Exception {
        try {
            OutputStream out = new VuaOutputStream("/tmp/TryWithResource.txt");
            Throwable outThrowable = null;
            try {
                TryIt tryIt = new TryIt();
                Throwable tryItThrowable = null;
                try {
                    System.out.println("About to do it");
                    out.write("Yay, it is working...".getBytes());
                    tryIt.doIt();
                } catch (Throwable localThrowable1) {
                    tryItThrowable = localThrowable1;
                    throw localThrowable1;
                } finally {
                    if (tryIt != null) {
                        if (tryItThrowable != null) {
                            try {
                                tryIt.close();
                            } catch (Throwable x2) {
                                tryItThrowable.addSuppressed(x2);
                            }
                        } else {
                            tryIt.close();
                        }
                    }
                }
            } catch (Throwable localThrowable2) {
                outThrowable = localThrowable2;
                throw localThrowable2;
            } finally {
                if (out != null) {
                    if (outThrowable != null) {
                        try {
                            out.close();
                        } catch (Throwable x2) {
                            outThrowable.addSuppressed(x2);
                        }
                    } else {
                        out.close();
                    }
                }
            }
        } catch (VuaException ex) {
            System.out.println("Bloody hell, I caught " + ex.getClass().getName());
            ex.printStackTrace();
            if (ex.getSuppressed().length == 0) {
                System.out.println("No suppressed exception");
            } else {
                System.out.println("Found suppressed exceptions...");
                for (Throwable throwable : ex.getSuppressed()) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    static class VuaOutputStream extends FileOutputStream {
        VuaOutputStream(String path) throws FileNotFoundException {
            super(path);
        }

        public void close() throws IOException {
            System.out.println("closing VuaOutputStream");
            super.close();
        }
    }

    static class VuaException extends Exception {
        VuaException() {
            super();
        }
    }

    static class TryIt implements AutoCloseable {
        public TryIt() {
            throw new RuntimeException("Won't let you create tryIt man");
        }

        public void doIt() throws TryWithResource.VuaException {
            System.out.println("TryWithResource$TryIt.doIt");
            System.out.println("About to throw IllegalArgumentException...");
            throw new IllegalArgumentException("DoIt shall not pass");
        }

        public void close() throws Exception {
            System.out.println("Closing tryIt instance");
            throw new RuntimeException("Unable to close");
        }
    }
}