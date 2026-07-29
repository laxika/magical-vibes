package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseCostOfSpellsTargetingThisSpellEffect;

@CardRegistration(set = "MIR", collectorNumber = "185")
public class KaerveksTorch extends Card {

    public KaerveksTorch() {
        // As long as Kaervek's Torch is on the stack, spells that target it cost {2} more to cast.
        addEffect(EffectSlot.STATIC, new IncreaseCostOfSpellsTargetingThisSpellEffect(2));

        // Kaervek's Torch deals X damage to any target.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue()));
    }
}
