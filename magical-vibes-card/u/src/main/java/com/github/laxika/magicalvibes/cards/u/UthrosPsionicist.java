package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReduceSecondSpellCastCostEffect;

@CardRegistration(set = "EOE", collectorNumber = "84")
public class UthrosPsionicist extends Card {

    public UthrosPsionicist() {
        addStaticEffect(new ReduceSecondSpellCastCostEffect(2));
    }
}
