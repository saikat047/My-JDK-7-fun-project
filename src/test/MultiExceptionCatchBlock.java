package test;

import java.security.AccessControlException;

/**
 * @author <a href="mailto:saikat@viatravel.no">Kazi Abdullah Saikat</a>
 */
public class MultiExceptionCatchBlock {
    public static void main(String[] pArgv) throws Exception {
        try {
            System.out.println("Do it sucker...");
        } catch (final IllegalArgumentException | AccessControlException aex) {
            aex.printStackTrace();
            throw aex;
        }
    }
}
