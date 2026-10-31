package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReduceSecondSpellCastCostEffect;

@CardRegistration(set = "TDM", collectorNumber = "47")
public class HighspireBellRinger extends Card {

    public HighspireBellRinger() {
        addStaticEffect(new ReduceSecondSpellCastCostEffect(1));
    }
}
