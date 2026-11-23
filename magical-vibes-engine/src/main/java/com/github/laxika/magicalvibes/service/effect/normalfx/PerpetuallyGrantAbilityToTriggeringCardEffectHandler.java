package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilityToTriggeringCardEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link PerpetuallyGrantAbilityToTriggeringCardEffect}: grants the ability to the triggering card's identity
 * when the trigger names one (the land that entered, the creature card put into the graveyard, the equipped creature
 * that died), else to the ability's own card. Each resolution adds its own instance.
 */
@Component
public class PerpetuallyGrantAbilityToTriggeringCardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantAbilityToTriggeringCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getCard() == null) {
            return;
        }
        var grant = (PerpetuallyGrantAbilityToTriggeringCardEffect) effect;
        UUID grantedCardId = entry.getTriggeringCardId() != null
                ? entry.getTriggeringCardId() : entry.getCard().getId();
        gameData.grantPerpetualAbility(grantedCardId, grant.grantedAbility());
    }
}
