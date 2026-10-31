package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "SUM", collectorNumber = "126")
@CardRegistration(set = "3ED", collectorNumber = "126")
@CardRegistration(set = "2ED", collectorNumber = "125")
@CardRegistration(set = "SPG", collectorNumber = "69")
@CardRegistration(set = "LEA", collectorNumber = "124")
@CardRegistration(set = "LEB", collectorNumber = "125")
public class Sacrifice extends Card {

    public Sacrifice() {
        addCastingCost(new SacrificeCreatureCost(true));
        addSpellEffect(new AwardManaEffect(ManaColor.BLACK, new XValue()));
    }
}
