import main.java.service.RedisEmulator;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        RedisEmulator emulator = new RedisEmulator();
        emulator.set("name", "nikhil", 5);
        System.out.println(emulator.get("name"));
        Thread.sleep(6000);
        System.out.println(emulator.get("name"));
    }
}