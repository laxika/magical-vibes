package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalLoyaltyCountersToCastSpellEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GrantAdditionalLoyaltyCountersToCastSpellEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAdditionalLoyaltyCountersToCastSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantAdditionalLoyaltyCountersToCastSpellEffect grant =
                (GrantAdditionalLoyaltyCountersToCastSpellEffect) effect;
        UUID spellCardId = entry.getTriggeringCardId();
        if (spellCardId == null || grant.amount() <= 0) {
            return;
        }

        for (StackEntry spellEntry : gameData.stack) {
            if (spellCardId.equals(spellEntry.getCard().getId())) {
                spellEntry.setGrantedAdditionalLoyaltyCounters(
                        spellEntry.getGrantedAdditionalLoyaltyCounters() + grant.amount());
                return;
            }
        }
    }
}
