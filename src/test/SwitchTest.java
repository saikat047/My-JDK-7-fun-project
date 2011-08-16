package test;

/**
 * @author <a href="mailto:saikat@viatravel.no">Kazi Abdullah Saikat</a>
 */
public class SwitchTest {

    private static final String [] values = {"saikat", "shishir", "kutu"};

    public static void main(String [] pArgv) {
        if (pArgv.length != 1) {
            System.out.println("usage: program test-string");
            System.exit(-1);
        }

        switch (pArgv[0]) {
            case "Saikat":
                System.out.println("User chose capitalized  'saikat'");
                break;
            case "saikat":
                System.out.println("User chose 'saikat'");
                break;
            case "shishir":
                System.out.println("User chose 'shishir'");
                break;
            case "kutu":
                System.out.println("User chose 'kutu'");
                break;
            default:
                System.out.println("Unknown option chosen");
                break;
        }
    }
}
