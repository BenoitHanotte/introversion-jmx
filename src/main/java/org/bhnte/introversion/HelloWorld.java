package org.bhnte.introversion;

public class HelloWorld {

    public static void main(String [] args) {
        System.out.println("Will do nothing for 10sec");
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done!");
    }
}
