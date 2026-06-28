package com.github.laxika.magicalvibes;

import com.github.laxika.magicalvibes.config.GameEngineConfig;
import com.github.laxika.magicalvibes.service.CardBrowserService;
import com.github.laxika.magicalvibes.service.DeckService;
import com.github.laxika.magicalvibes.service.DraftService;
import com.github.laxika.magicalvibes.service.LoginService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {
        "com.github.laxika.magicalvibes.handler",
        "com.github.laxika.magicalvibes.repository",
        "com.github.laxika.magicalvibes.ai",
        "com.github.laxika.magicalvibes.websocket",
        "com.github.laxika.magicalvibes.config"
})
@Import(GameEngineConfig.class)
@ComponentScan(basePackageClasses = {
        LoginService.class,
        DeckService.class,
        DraftService.class,
        CardBrowserService.class
})
public class MagicalVibesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicalVibesApplication.class, args);
    }
}

