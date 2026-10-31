package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

public class AllureOfPower extends Card {

    public AllureOfPower() {
        addCastingCost(new SacrificeCreatureCost());
        addSpellEffect(new DrawCardEffect(2));
    }
}
