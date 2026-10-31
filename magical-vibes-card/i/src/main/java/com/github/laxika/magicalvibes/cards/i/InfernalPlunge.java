package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "ISD", collectorNumber = "148")
public class InfernalPlunge extends Card {

    public InfernalPlunge() {
        addCastingCost(new SacrificeCreatureCost());
        addSpellEffect(new AwardManaEffect(ManaColor.RED, 3));
    }
}
