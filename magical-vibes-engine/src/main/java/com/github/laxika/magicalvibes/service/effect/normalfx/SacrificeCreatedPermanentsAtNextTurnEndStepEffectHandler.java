package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.SacrificeSelfAtNextEndStepTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtNextTurnEndStepEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link SacrificeCreatedPermanentsAtNextTurnEndStepEffect} by recording each created
 * permanent and the turn in which the delayed ability was registered.
 */
@Component
@Slf4j
public class SacrificeCreatedPermanentsAtNextTurnEndStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeCreatedPermanentsAtNextTurnEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID createdId : entry.getCreatedPermanentIds()) {
            gameData.queueDelayedAction(new SacrificeSelfAtNextEndStepTrigger(
                    createdId, entry.getControllerId(), entry.getCard(), gameData.turnNumber));
        }
        log.info("Game {} - {} permanent(s) scheduled for sacrifice at {}'s next turn end step",
                gameData.id, entry.getCreatedPermanentIds().size(),
                gameData.playerIdToName.get(entry.getControllerId()));
    }
}
