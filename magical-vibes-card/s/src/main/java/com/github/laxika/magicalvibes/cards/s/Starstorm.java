package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "ONS", collectorNumber = "238")
@CardRegistration(set = "VMA", collectorNumber = "189")
@CardRegistration(set = "C13", collectorNumber = "122")
@CardRegistration(set = "C14", collectorNumber = "182")
@CardRegistration(set = "40K", collectorNumber = "208")
@CardRegistration(set = "C20", collectorNumber = "160")
@CardRegistration(set = "BLC", collectorNumber = "203")
@CardRegistration(set = "NEC", collectorNumber = "110")
@CardRegistration(set = "CM2", collectorNumber = "118")
public class Starstorm extends Card {

    public Starstorm() {
        addSpellEffect(DealDamageEffect.toEachCreature(new XValue()));
        addCycling("{3}");
    }
}
