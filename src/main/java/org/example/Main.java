package org.example;


import lombok.extern.log4j.Log4j2;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;

import java.util.Timer;
import java.util.TimerTask;

@Log4j2
public class Main {
    public static void main(String[] args) {
        log.info("Hello world!");
        new Timer().schedule(new Task(), 0, 1000);
        log.warn("test");
        log.error("test");
        log.trace("test");
        log.debug("test");
        log.fatal("test");
        CustomAppender customAppender = CustomAppender.getInstance();
        if(customAppender!=null){
            LineReader lineReader = customAppender.getReader();
            String prompt = "> ";
            String line;
            try {
                do {
                    line = lineReader.readLine(prompt);
                    if (line.isEmpty()) {
                        continue;
                    }
                    log.info("You entered: " + line);
                } while (!"exit".equalsIgnoreCase(line.trim()));
            } catch (UserInterruptException | EndOfFileException ignored) {
                // Ctrl+C for closing application
            }
            log.info("Application is closing.");
            System.exit(0);
        }
    }

    private static class Task extends TimerTask {
        int count = 1;
        public void run() {
            log.info(count++);
        }
    }
}