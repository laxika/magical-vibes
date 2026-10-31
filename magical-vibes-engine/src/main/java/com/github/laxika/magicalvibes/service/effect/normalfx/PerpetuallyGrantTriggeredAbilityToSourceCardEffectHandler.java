package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToSourceCardEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Records a perpetual triggered-ability grant on the triggering card's identity when the trigger
 * names one (an equipped creature that died), else on the source card. Each resolution adds its own
 * independent copy of the ability.
 */
@Component
public class PerpetuallyGrantTriggeredAbilityToSourceCardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantTriggeredAbilityToSourceCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getCard() == null) {
            return;
        }
        var grant = (PerpetuallyGrantTriggeredAbilityToSourceCardEffect) effect;
        UUID grantedCardId = entry.getTriggeringCardId() != null
                ? entry.getTriggeringCardId() : entry.getCard().getId();
        gameData.grantPerpetualAbility(grantedCardId, grant.triggeredAbilitySlot(), grant.triggeredAbility());
    }
}
