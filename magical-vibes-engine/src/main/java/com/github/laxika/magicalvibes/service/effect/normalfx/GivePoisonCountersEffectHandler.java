package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the whole give-poison family via {@link GivePoisonCountersEffect}: the
 * {@link com.github.laxika.magicalvibes.model.effect.PoisonRecipient} routes who gets the poison
 * counters. All placement goes through the central {@link LifeSupport#applyPoisonCounters} path so
 * poison-suppression (Melira / {@code PlayerCantGetPoisonCountersEffect}), infect interactions and
 * poison-loss checks keep working uniformly.
 */
@Component
@RequiredArgsConstructor
public class GivePoisonCountersEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GivePoisonCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GivePoisonCountersEffect) effect;
        String sourceName = entry.getCard().getName();

        switch (e.recipient()) {
            case CONTROLLER -> lifeSupport.applyPoisonCounters(gameData, entry.getControllerId(), e.amount(), sourceName);
            case TARGET_PLAYER -> {
                UUID targetPlayerId = entry.getTargetId();
                if (targetPlayerId == null) return;
                lifeSupport.applyPoisonCounters(gameData, targetPlayerId, e.amount(), sourceName);
            }
            case EACH_PLAYER -> {
                for (UUID playerId : gameData.orderedPlayerIds) {
                    lifeSupport.applyPoisonCounters(gameData, playerId, e.amount(), sourceName);
                }
            }
            case ENCHANTED_PERMANENT_CONTROLLER -> {
                UUID playerId = e.affectedPlayerId();
                if (playerId == null) return;
                lifeSupport.applyPoisonCounters(gameData, playerId, e.amount(), sourceName);
            }
        }
    }
}
