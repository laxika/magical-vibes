package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "34")
@CardRegistration(set = "INV", collectorNumber = "31")
public class RevivingDose extends Card {

    public RevivingDose() {
        addSpellEffect(new GainLifeEffect(3));
        addSpellEffect(new DrawCardEffect());
    }
}
