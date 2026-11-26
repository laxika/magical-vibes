package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "173")
@CardRegistration(set = "P02", collectorNumber = "133")
@CardRegistration(set = "8ED", collectorNumber = "266")
@CardRegistration(set = "7ED", collectorNumber = "258")
@CardRegistration(set = "S99", collectorNumber = "132")
public class MonstrousGrowth extends Card {

    public MonstrousGrowth() {
        addSpellEffect(BoostEffect.target(4, 4));
    }
}
