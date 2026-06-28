package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerTestFixtures;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
abstract class AbstractStaticEffectHandlerTest {

    @Mock protected GameQueryService gameQueryService;

    protected StaticEffectHandlerRegistry registry;
    protected GameData gd;
    protected UUID player1Id;
    protected UUID player2Id;

    @BeforeEach
    void setUpStaticEffectHandlerBase() {
        StaticEffectSupport support = new StaticEffectSupport(gameQueryService);
        registry = new StaticEffectHandlerRegistry();
        StaticEffectHandlerBeanFactory.registerAll(
                StaticEffectHandlerBeanFactory.createAll(support, gameQueryService, registry),
                registry);

        var game = EffectHandlerTestFixtures.newTwoPlayerGameData(false);
        player1Id = game.player1Id();
        player2Id = game.player2Id();
        gd = game.gameData();
    }
}
