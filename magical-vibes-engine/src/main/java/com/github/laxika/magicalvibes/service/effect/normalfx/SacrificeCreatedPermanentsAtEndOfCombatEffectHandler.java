package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndOfCombatEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.service.battlefield.DelayedPermanentActions;

/**
 * Schedules the permanents created earlier in the same stack-entry resolution for end-of-combat
 * sacrifice.
 */
@Component
@Slf4j
public class SacrificeCreatedPermanentsAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeCreatedPermanentsAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (var createdId : entry.getCreatedPermanentIds()) {
            DelayedPermanentActions.schedule(gameData, entry,
                    new DelayedPermanentAction(createdId, DelayedPermanentActionKind.SACRIFICE_AT_END_OF_COMBAT),
                    ActivePlayer.ANY);
        }
        log.info("Game {} - {} permanent(s) scheduled for sacrifice at end of combat by {}",
                gameData.id, entry.getCreatedPermanentIds().size(), entry.getCard().getName());
    }
}
