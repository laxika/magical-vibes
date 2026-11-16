package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreatedPermanentsAtControllerEndStepEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Resolves {@link ExileCreatedPermanentsAtControllerEndStepEffect} by recording the controller of
 * the resolving entry, so the delayed action skips the opponent's end step.
 */
@Component
@Slf4j
public class ExileCreatedPermanentsAtControllerEndStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCreatedPermanentsAtControllerEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        for (UUID createdId : entry.getCreatedPermanentIds()) {
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                    Triggers.endStep(ActivePlayer.YOU), controllerId, entry.getCard(), entry.getSourcePermanentId(),
                    createdId, new ExileTargetPermanentEffect()));
        }
        log.info("Game {} - {} permanent(s) scheduled for exile at {}'s next end step",
                gameData.id, entry.getCreatedPermanentIds().size(),
                gameData.playerIdToName.get(controllerId));
    }
}
