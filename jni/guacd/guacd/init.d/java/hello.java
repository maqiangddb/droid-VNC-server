import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;
import java.net.Socket;

public class hello {
    static final String IP_ADD= "192.168.1.60";//"127.0.0.1";//"192.168.191.2";
    static final int PORT = 4822;
	public static void main (String[] args) {
		System.out.println("hello word!");
        try {
            Socket client = new Socket(IP_ADD, PORT);
            System.out.println("new an Socket");
            client.setSoTimeout(10000);

            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("get Input buffer");

            PrintStream out = new PrintStream(client.getOutputStream());

            out.println("6.select,3.vnc;");

            BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()));

            boolean flag = true;

            while (flag) {
                System.out.println("input msg:");
                String str = input.readLine();
                if ("bye".equals(str)) {
                    flag = false;
                } else {
                    out.println(str);
                }


                String echo = buf.readLine();
                System.out.println(echo);


                input.close();
                if (client != null) {
                    client.close();
                }
            }
        } catch (Exception e) {
        	System.out.println("get an Exception!!!!");
            e.printStackTrace();
        }

	}
}