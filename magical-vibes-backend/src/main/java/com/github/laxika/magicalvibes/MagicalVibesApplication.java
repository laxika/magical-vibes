package com.github.laxika.magicalvibes;

import com.github.laxika.magicalvibes.config.GameEngineConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {
        "com.github.laxika.magicalvibes.handler",
        "com.github.laxika.magicalvibes.repository",
        "com.github.laxika.magicalvibes.ai",
        "com.github.laxika.magicalvibes.websocket",
        "com.github.laxika.magicalvibes.config",
        "com.github.laxika.magicalvibes.webservice"
})
@Import(GameEngineConfig.class)
public class MagicalVibesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicalVibesApplication.class, args);
    }
}

