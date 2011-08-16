package test;

import java.io.*;

/**
 * @author <a href="mailto:saikat@viatravel.no">Kazi Abdullah Saikat</a>
 */
public class TryWithHecticResource {
    public static void main(String [] argv) {
        int i = 1_2_3;
        new TryWithHecticResource().testIt();
    }

    void testIt() {
        try (OutputStream os = new NormalFileOS("/tmp/TryIt.txt");ExBufferedOS buffOs = new ExBufferedOS(os);) {
            buffOs.write("is this ok?\n".getBytes());
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            if (ioEx.getSuppressed().length != 0) {
                System.out.println("found suppressed");
                for (Throwable throwable : ioEx.getSuppressed()) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    class NormalFileOS extends FileOutputStream {
        NormalFileOS(String path) throws FileNotFoundException {
            super(path);
            System.out.println("TryWithHecticResource$NormalFileOS.NormalFileOS");
        }

        @Override
        public void close() throws IOException {
            System.out.println("TryWithHecticResource$NormalFileOS.close");
            super.close();
        }
    }

    class ExBufferedOS extends BufferedOutputStream {
        ExBufferedOS(OutputStream os) throws IOException {
            super(os);
            System.out.println("TryWithHecticResource$ExBufferedOS.ExBufferedOS");
            throw new IOException("Won't let you do it");
        }

        @Override
        public void close() throws IOException {
            System.out.println("TryWithHecticResource$ExBufferedOS.close");
            super.close();
        }
    }
}
