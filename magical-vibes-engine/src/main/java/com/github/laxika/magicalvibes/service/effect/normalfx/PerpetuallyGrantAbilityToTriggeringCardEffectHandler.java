package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
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
        // A permanent that entered as a copy of another card (Vesuva) names its copied card; the perpetual change is
        // made to its own physical card, the one that keeps it in every zone.
        Permanent entered = entry.getAttachedPermanentSnapshot();
        if (entered != null && entered.getId().equals(entry.getTriggeringPermanentId())
                && entered.getOriginalCard() != null && entered.getCard().getId().equals(grantedCardId)) {
            grantedCardId = entered.getOriginalCard().getId();
        }
        gameData.grantPerpetualAbility(grantedCardId, grant.grantedAbility());
    }
}
