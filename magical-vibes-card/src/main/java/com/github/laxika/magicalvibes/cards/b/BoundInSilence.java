package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FUT", collectorNumber = "20")
public class BoundInSilence extends Card {

    public BoundInSilence() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect());
    }
}
