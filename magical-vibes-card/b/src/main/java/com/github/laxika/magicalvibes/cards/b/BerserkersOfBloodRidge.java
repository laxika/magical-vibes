package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "126")
@CardRegistration(set = "M11", collectorNumber = "124")
public class BerserkersOfBloodRidge extends Card {

    public BerserkersOfBloodRidge() {
        addStaticEffect(new MustAttackEffect());
    }
}
