package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleAllOwnCreaturesPowerToughnessEffect;

@CardRegistration(set = "INR", collectorNumber = "223")
public class UnnaturalGrowth extends Card {

    public UnnaturalGrowth() {
        // At the beginning of each combat, double the power and toughness of each creature you
        // control until end of turn.
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                new DoubleAllOwnCreaturesPowerToughnessEffect());
    }
}
