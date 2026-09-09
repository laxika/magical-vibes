package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "SCG", collectorNumber = "83")
public class Carbonize extends Card {

    public Carbonize() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new Fixed(3), true, true)
                .withUnconditionalRegenerationPrevention());
    }
}
