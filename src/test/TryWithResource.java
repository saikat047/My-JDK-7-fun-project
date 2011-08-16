package test;

import java.io.*;

/**
 * @author <a href="mailto:saikat@viatravel.no">Kazi Abdullah Saikat</a>
 */
public class TryWithResource {

    public static void main(String[] pArgv) throws Exception {
        try (OutputStream out = new VuaOutputStream(new FileOutputStream("/tmp/TryWithResource.txt")); TryIt tryIt = new TryIt()) {
            System.out.println("About to do it");
            out.write("Yay, it is working...".getBytes());
            tryIt.doIt();
        } catch (final VuaException | RuntimeException ex) {
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

    static class TryIt implements AutoCloseable {
        public TryIt() {
            throw new RuntimeException("Won't let you create tryIt man");
            // System.out.println("Creating a new instance of TryIt");
        }

        public void doIt() throws VuaException {
            System.out.println("TryWithResource$TryIt.doIt");
            System.out.println("About to throw IllegalArgumentException...");
            throw new IllegalArgumentException("DoIt shall not pass");
        }

        @Override
        public void close() throws Exception {
            System.out.println("Closing tryIt instance");
            throw new RuntimeException("Unable to close");
        }
    }

    static class VuaException extends Exception {
        VuaException() {
            super("This is a vua-exception");
        }
    }

    static class VuaOutputStream extends BufferedOutputStream {
        VuaOutputStream(final OutputStream out) {
            super(out);
        }

        @Override
        public void close() throws IOException {
            System.out.println("closing VuaOutputStream");
            super.close();
        }
    }
}
