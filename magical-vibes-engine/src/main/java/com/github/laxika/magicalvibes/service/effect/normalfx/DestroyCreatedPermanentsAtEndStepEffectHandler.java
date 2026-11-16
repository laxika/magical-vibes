package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatedPermanentsAtEndStepEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.service.battlefield.DelayedPermanentActions;

/**
 * Queues destruction for permanents created by the current stack-entry resolution. The delayed trigger
 * belongs to the ability that created the permanents, so it is controlled by that ability's controller
 * as it resolved, even if the created permanents change control (CR 603.7e).
 */
@Component
@Slf4j
public class DestroyCreatedPermanentsAtEndStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyCreatedPermanentsAtEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID createdId : entry.getCreatedPermanentIds()) {
            DelayedPermanentActions.schedule(gameData, entry,
                    new DelayedPermanentAction(createdId, DelayedPermanentActionKind.DESTROY_AT_END_STEP),
                    ActivePlayer.ANY);
        }
        log.info("Game {} - {} permanent(s) scheduled for destruction at end step by {}",
                gameData.id, entry.getCreatedPermanentIds().size(), entry.getCard().getName());
    }
}
