package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTJ", collectorNumber = "155")
public class BetrayalAtTheVault extends Card {

    public BetrayalAtTheVault() {
        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new TargetCreatureDealsPowerDamageToAnyTargetEffect(0, 1));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new TargetCreatureDealsPowerDamageToAnyTargetEffect(0, 2));
    }
}
