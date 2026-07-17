package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDamagedCreatureUnderControlEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Seraph's {@code ON_DAMAGED_CREATURE_DIES} trigger: schedules the dying creature's card
 * to return to the battlefield under Seraph's controller at the beginning of the next end step,
 * linked to the source Seraph for the control-loss sacrifice.
 */
@Slf4j
@Component
public class RegisterDelayedReturnDamagedCreatureUnderControlEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnDamagedCreatureUnderControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTriggeringCardId() == null || entry.getSourcePermanentId() == null) {
            return;
        }
        gameData.queueDelayedAction(new DelayedGraveyardToBattlefieldUnderControl(
                entry.getTriggeringCardId(), entry.getControllerId(), entry.getSourcePermanentId()));
        log.info("Game {} - {} schedules a damaged creature to return under control at the next end step",
                gameData.id, entry.getCard().getName());
    }
}
