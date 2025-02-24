import java.io.*;
import java.net.*;

public class Cliente {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado ao Caixa Eletrônico!");
            System.out.println(in.readLine()); // Mensagem de boas-vindas

            // Autenticação
            System.out.print("Usuário: ");
            String user = consoleInput.readLine();
            out.println(user);

            System.out.print("Senha: ");
            String pass = consoleInput.readLine();
            out.println(pass);

            String response = in.readLine();
            System.out.println(response);

            if (response.contains("Falha")) {
                return;
            }

            // Loop de comandos
            while (true) {
                System.out.println("\nOpções: [DEPOSITAR valor] [SACAR valor] [CONSULTAR_SALDO] [SAIR]");
                System.out.print("Digite um comando: ");
                String command = consoleInput.readLine();
                out.println(command);

                if (command.equalsIgnoreCase("SAIR")) {
                    break;
                }

                System.out.println(in.readLine());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

