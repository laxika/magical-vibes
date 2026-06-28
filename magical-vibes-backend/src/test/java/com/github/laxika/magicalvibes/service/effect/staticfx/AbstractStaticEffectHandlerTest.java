package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerTestFixtures;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
abstract class AbstractStaticEffectHandlerTest {

    @Mock protected GameQueryService gameQueryService;

    protected StaticEffectHandlerRegistry registry;
    protected com.github.laxika.magicalvibes.model.GameData gd;
    protected UUID player1Id;
    protected UUID player2Id;

    @BeforeEach
    void setUpStaticEffectHandlerBase() {
        StaticEffectSupport support = new StaticEffectSupport(gameQueryService);
        registry = new StaticEffectHandlerRegistry();
        StaticEffectHandlerBean handler = instantiateHandlerUnderTest(support, gameQueryService);
        if (handler.selfOnly()) {
            registry.registerSelfHandler(handler.handledEffect(), handler);
        } else {
            registry.register(handler.handledEffect(), handler);
        }

        var game = EffectHandlerTestFixtures.newTwoPlayerGameData(false);
        player1Id = game.player1Id();
        player2Id = game.player2Id();
        gd = game.gameData();
    }

    private StaticEffectHandlerBean instantiateHandlerUnderTest(StaticEffectSupport support,
                                                                GameQueryService gameQueryService) {
        String handlerName = getClass().getSimpleName().replace("Test", "");
        try {
            Class<?> handlerClass = Class.forName(getClass().getPackageName() + "." + handlerName);
            Map<Class<?>, Object> deps = new HashMap<>();
            deps.put(StaticEffectSupport.class, support);
            deps.put(GameQueryService.class, gameQueryService);
            deps.put(StaticEffectHandlerRegistry.class, registry);

            for (Constructor<?> constructor : handlerClass.getConstructors()) {
                Object[] args = new Object[constructor.getParameterCount()];
                Class<?>[] paramTypes = constructor.getParameterTypes();
                boolean ok = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    Object dep = deps.get(paramTypes[i]);
                    if (dep == null) {
                        ok = false;
                        break;
                    }
                    args[i] = dep;
                }
                if (ok) {
                    return (StaticEffectHandlerBean) constructor.newInstance(args);
                }
            }
            throw new IllegalStateException("No matching constructor for " + handlerName);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate static handler for " + getClass().getSimpleName(), e);
        }
    }
}
