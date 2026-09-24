package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ONS", collectorNumber = "234")
@CardRegistration(set = "VMA", collectorNumber = "187")
@CardRegistration(set = "DMR", collectorNumber = "140")
public class SolarBlast extends Card {

    public SolarBlast() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

        addCycling("{1}{R}{R}");
        addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(
                new DealDamageToAnyTargetEffect(1), "Deal 1 damage to the chosen target?"));
    }
}
