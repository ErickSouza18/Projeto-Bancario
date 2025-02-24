import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

class Account {
    private String username;
    private String password;
    private double balance;

    public Account(String username, String password, double balance) {
        this.username = username;
        this.password = password;
        this.balance = balance;
    }

    public boolean authenticate(String user, String pass) {
        return this.username.equals(user) && this.password.equals(pass);
    }

    public synchronized boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public synchronized void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    public synchronized double getBalance() {
        return balance;
    }
}

public class Servidor {
    private static final int PORT = 12345;
    private static Map<Integer, Account> accounts = new HashMap<>();

    public static void main(String[] args) {
        // Criando contas iniciais
        accounts.put(1001, new Account("user1", "pass1", 5000.0));
        accounts.put(1002, new Account("user2", "pass2", 3000.0));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor do Caixa Eletrônico iniciado na porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private Account currentAccount;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("Bem-vindo ao Caixa Eletrônico! Informe usuário e senha:");

                // Autenticação
                out.print("Usuário: ");
                out.flush();
                String user = in.readLine();

                out.print("Senha: ");
                out.flush();
                String pass = in.readLine();

                for (Map.Entry<Integer, Account> entry : accounts.entrySet()) {
                    if (entry.getValue().authenticate(user, pass)) {
                        currentAccount = entry.getValue();
                        out.println("Autenticação bem-sucedida! Conta: " + entry.getKey());
                        break;
                    }
                }

                if (currentAccount == null) {
                    out.println("Falha na autenticação! Encerrando conexão...");
                    socket.close();
                    return;
                }

                String command;
                while ((command = in.readLine()) != null) {
                    String[] parts = command.split(" ");
                    switch (parts[0].toUpperCase()) {
                        case "DEPOSITAR":
                            double depositAmount = Double.parseDouble(parts[1]);
                            currentAccount.deposit(depositAmount);
                            out.println("Depósito de R$ " + depositAmount + " realizado com sucesso.");
                            break;
                        case "SACAR":
                            double withdrawAmount = Double.parseDouble(parts[1]);
                            if (currentAccount.withdraw(withdrawAmount)) {
                                out.println("Saque de R$ " + withdrawAmount + " realizado com sucesso.");
                            } else {
                                out.println("Saldo insuficiente ou valor inválido.");
                            }
                            break;
                        case "CONSULTAR_SALDO":
                            out.println("Seu saldo é: R$ " + currentAccount.getBalance());
                            break;
                        case "SAIR":
                            out.println("Obrigado por usar o Caixa Eletrônico!");
                            socket.close();
                            return;
                        default:
                            out.println("Comando inválido!");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
