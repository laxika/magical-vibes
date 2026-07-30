package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "AVR", collectorNumber = "160")
public class ThunderousWrath extends Card {

    public ThunderousWrath() {
        // Miracle {R}
        addCastingOption(new MiracleCast("{R}"));

        // Thunderous Wrath deals 5 damage to any target.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(5));
    }
}
