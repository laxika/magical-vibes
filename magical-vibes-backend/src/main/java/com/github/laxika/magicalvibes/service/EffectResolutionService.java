package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EffectResolutionService {

    private final GameHelper gameHelper;
    private final List<EffectHandlerProvider> providers;

    private EffectHandlerRegistry registry;

    @PostConstruct
    public void init() {
        registry = new EffectHandlerRegistry();
        providers.forEach(p -> p.registerHandlers(registry));
    }

    void resolveEffects(GameData gameData, StackEntry entry) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (CardEffect effect : effects) {
            EffectHandler handler = registry.getHandler(effect);
            if (handler != null) {
                handler.resolve(gameData, entry, effect);
            } else {
                log.warn("No handler for effect: {}", effect.getClass().getSimpleName());
            }
            if (gameData.interaction.awaitingInput != null || !gameData.pendingMayAbilities.isEmpty()) {
                break;
            }
        }
        gameHelper.removeOrphanedAuras(gameData);
    }
}

