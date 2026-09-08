package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndOfCombatEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
            gameData.queueDelayedAction(new SacrificeAtEndOfCombat(createdId,
                    entry.getControllerId(), entry.getCard(), 0));
        }
        log.info("Game {} - {} permanent(s) scheduled for sacrifice at end of combat by {}",
                gameData.id, entry.getCreatedPermanentIds().size(), entry.getCard().getName());
    }
}
