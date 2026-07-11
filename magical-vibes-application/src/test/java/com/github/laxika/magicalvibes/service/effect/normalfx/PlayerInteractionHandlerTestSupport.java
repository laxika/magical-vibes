package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Instantiates a single {@link NormalEffectHandlerBean} for unit tests using mocked collaborators.
 */
final class PlayerInteractionHandlerTestSupport {

    private PlayerInteractionHandlerTestSupport() {
    }

    static NormalEffectHandlerBean createHandler(String handlerSimpleName,
                                                 PlayerInteractionSupport support,
                                                 EffectHandlerRegistry registry,
                                                 GameBroadcastService gameBroadcastService,
                                                 DrawService drawService,
                                                 SessionManager sessionManager,
                                                 CardViewFactory cardViewFactory,
                                                 GameQueryService gameQueryService,
                                                 com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService predicateEvaluationService,
                                                 PlayerInputService playerInputService,
                                                 TriggerCollectionService triggerCollectionService,
                                                 BattlefieldEntryService battlefieldEntryService,
                                                 PermanentRemovalService permanentRemovalService,
                                                 GraveyardService graveyardService,
                                                 InteractionHandlerRegistry interactionHandlerRegistry) {
        try {
            Class<?> handlerClass = Class.forName(
                    PlayerInteractionHandlerTestSupport.class.getPackageName() + "." + handlerSimpleName);
            Map<Class<?>, Object> deps = new HashMap<>();
            deps.put(PlayerInteractionSupport.class, support);
            deps.put(EffectHandlerRegistry.class, registry);
            deps.put(GameBroadcastService.class, gameBroadcastService);
            deps.put(DrawService.class, drawService);
            deps.put(SessionManager.class, sessionManager);
            deps.put(CardViewFactory.class, cardViewFactory);
            deps.put(GameQueryService.class, gameQueryService);
            deps.put(com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService.class, predicateEvaluationService);
            deps.put(PlayerInputService.class, playerInputService);
            deps.put(TriggerCollectionService.class, triggerCollectionService);
            deps.put(BattlefieldEntryService.class, battlefieldEntryService);
            deps.put(PermanentRemovalService.class, permanentRemovalService);
            deps.put(GraveyardService.class, graveyardService);
            deps.put(InteractionHandlerRegistry.class, interactionHandlerRegistry);
            // Real amount evaluator on top of the mocked collaborators — Fixed amounts
            // evaluate without touching any mock.
            deps.put(com.github.laxika.magicalvibes.service.effect.AmountEvaluationService.class,
                    new com.github.laxika.magicalvibes.service.effect.AmountEvaluationService(
                            predicateEvaluationService, gameQueryService));

            Constructor<?> chosen = null;
            Object[] chosenArgs = null;
            search:
            for (Constructor<?> constructor : handlerClass.getConstructors()) {
                Object[] args = new Object[constructor.getParameterCount()];
                Class<?>[] paramTypes = constructor.getParameterTypes();
                for (int i = 0; i < paramTypes.length; i++) {
                    Object dep = deps.get(paramTypes[i]);
                    if (dep == null) {
                        continue search;
                    }
                    args[i] = dep;
                }
                chosen = constructor;
                chosenArgs = args;
                break;
            }
            if (chosen == null) {
                throw new IllegalStateException("No constructor with mocked deps for " + handlerSimpleName);
            }
            return (NormalEffectHandlerBean) chosen.newInstance(chosenArgs);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate handler " + handlerSimpleName, e);
        }
    }

    static void registerHandler(EffectHandlerRegistry registry, NormalEffectHandlerBean handler) {
        registry.register(handler.handledEffect(), (EffectHandler) handler);
    }

    static Class<? extends CardEffect> handledEffect(NormalEffectHandlerBean handler) {
        return handler.handledEffect();
    }
}
