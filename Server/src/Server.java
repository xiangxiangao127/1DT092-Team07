import robot.RobotControl;
import robot.RobotMessage;
import robot.RobotMessageException;
import robot.RobotTest;
import warehouse.Warehouse;
import warehouse.WarehousePackage;
import warehouse.WarehouseRobot;

import java.awt.*;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

/*
 * Main entry point for the server.
 **/
public class Server {

    private static Warehouse warehouse;
    private static Server server;
    private static RobotControl robotControl;

    private static Thread serverThread;
    private static Thread serverControlThread;

    private static boolean isAlive;

    private Server() {
        warehouse = new Warehouse(16, 16);
        warehouse.addObject(new WarehousePackage(0, 0));
        warehouse.addObject(new WarehouseRobot(1, 0));
        warehouse.addPath(3, 3, 6, 3);
        warehouse.addPath(6, 3, 6, 9);

        robotControl = new RobotControl();
        robotControl.initialize(5555);
    }

    public static void main(String[] args) {

        try {
            RobotMessage msg = RobotMessage.decodeMessage(RobotMessage.encodeMessage(new RobotMessage(1, "Test")));
            System.out.println("Op: " + msg.getOp() + " Data: " + msg.getData());
        } catch (RobotMessageException e) {
            System.err.println(e.getMessage());
        }

        if (true) {

            server = new Server();
            serverThread = server.start();

            serverControlThread = new Thread(() -> {
                Scanner sc = new Scanner(System.in);

                while (isAlive) {
                    String cmd = sc.nextLine();

                    if (cmd.equals("quit")) {
                        isAlive = false;
                    }
                }
            });
            serverControlThread.start();

            /*
             * Test robots
             */
            RobotTest robotTest = new RobotTest("127.0.0.1", 5555);
            robotTest.connect();

            try {
                serverThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    private Thread start() {
        isAlive = true;

        Thread thread = new Thread(() -> {
            while (isAlive) {
                try {
                    RobotMessage msg = robotControl.pollMessage();
                    if (msg != null) {
                        System.out.println("[Server] Data from RobotControl: " + msg);
                    }
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            robotControl.stop();
        });

        thread.start();

        return thread;
    }
}
