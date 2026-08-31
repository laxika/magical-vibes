package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceSecondSpellCastCostEffect;

@CardRegistration(set = "EOE", collectorNumber = "84")
public class UthrosPsionicist extends Card {

    public UthrosPsionicist() {
        addEffect(EffectSlot.STATIC, new ReduceSecondSpellCastCostEffect(2));
    }
}
