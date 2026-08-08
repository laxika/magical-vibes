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
        // The MayEffect flow (Shirei) does not carry the entry's triggering-card id, so the
        // collector binds the dying card onto the effect itself.
        var dyingCardId = e.dyingCardId() != null ? e.dyingCardId() : entry.getTriggeringCardId();
        // An emblem has no source permanent unless the delayed action must watch its control loss.
        if (dyingCardId == null
                || (e.sacrificeOnSourceControlLoss() && entry.getSourcePermanentId() == null)) {
            return;
        }
        gameData.queueDelayedAction(new DelayedGraveyardToBattlefieldUnderControl(
                dyingCardId, entry.getControllerId(), entry.getSourcePermanentId(),
                e.sacrificeOnSourceControlLoss(), e.counterType(), e.counterAmount(),
                e.grantColor(), e.grantSubtype(), e.requireSourceOnBattlefield()));
        log.info("Game {} - {} schedules a dying creature to return under control at the next end step",
                gameData.id, entry.getCard().getName());
    }
}
