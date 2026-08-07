package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the "put that card onto the battlefield under your control at the beginning of the next
 * end step" death triggers (Seraph, Grave Betrayal): schedules the dying creature's card to return
 * under the ability controller, carrying the effect's counter and color/subtype grant riders.
 */
@Slf4j
@Component
public class RegisterDelayedReturnDyingCreatureUnderControlEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnDyingCreatureUnderControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedReturnDyingCreatureUnderControlEffect) effect;
        // An emblem (Liliana, Defiant Necromancer's) has no source permanent — that is only needed
        // for the control-loss sacrifice link, so require it exclusively for that variant.
        if (entry.getTriggeringCardId() == null
                || (e.sacrificeOnSourceControlLoss() && entry.getSourcePermanentId() == null)) {
            return;
        }
        gameData.queueDelayedAction(new DelayedGraveyardToBattlefieldUnderControl(
                entry.getTriggeringCardId(), entry.getControllerId(), entry.getSourcePermanentId(),
                e.sacrificeOnSourceControlLoss(), e.counterType(), e.counterAmount(),
                e.grantColor(), e.grantSubtype()));
        log.info("Game {} - {} schedules a dying creature to return under control at the next end step",
                gameData.id, entry.getCard().getName());
    }
}
