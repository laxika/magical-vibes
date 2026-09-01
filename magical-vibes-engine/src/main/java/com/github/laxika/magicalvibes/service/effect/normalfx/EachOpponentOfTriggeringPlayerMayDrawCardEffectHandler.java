package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentOfTriggeringPlayerMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Queues an independent optional draw for every opponent of the spell's caster.
 */
@Component
public class EachOpponentOfTriggeringPlayerMayDrawCardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentOfTriggeringPlayerMayDrawCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringPlayerId = findTriggeringPlayerId(gameData, entry);
        if (triggeringPlayerId == null) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(triggeringPlayerId)) {
                continue;
            }
            gameData.queueMayAbilityForPlayer(
                    entry.getCard(),
                    entry.getControllerId(),
                    new MayEffect(new DrawCardForTargetPlayerEffect(1), "Draw a card?"),
                    playerId,
                    entry.getSourcePermanentId(),
                    playerId,
                    entry.getSourcePermanentSnapshot());
        }
    }

    private UUID findTriggeringPlayerId(GameData gameData, StackEntry entry) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId != null) {
            for (StackEntry stackEntry : gameData.stack) {
                if (stackEntry.getCard().getId().equals(triggeringCardId)) {
                    return stackEntry.getControllerId();
                }
            }
        }
        return entry.getTargetId();
    }
}
