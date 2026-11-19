package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtNextTurnEndStepEffect;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                    entry.getControllerId(), entry.getCard(),
                    new TriggeredAbility(Triggers.endStep(ActivePlayer.YOU), SacrificeSelfEffect.ofTriggeringPermanent()),
                    entry.getSourcePermanentId(), createdId, null,
                    DelayedReferents.permanents(List.of(createdId)).afterTurn(gameData.turnNumber)));
        }
        log.info("Game {} - {} permanent(s) scheduled for sacrifice at {}'s next turn end step",
                gameData.id, entry.getCreatedPermanentIds().size(),
                gameData.playerIdToName.get(entry.getControllerId()));
    }
}
