package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "NPH", collectorNumber = "116")
@CardRegistration(set = "MM2", collectorNumber = "149")
public class MutagenicGrowth extends Card {

    public MutagenicGrowth() {
        addSpellEffect(BoostEffect.target(2, 2));
    }
}
