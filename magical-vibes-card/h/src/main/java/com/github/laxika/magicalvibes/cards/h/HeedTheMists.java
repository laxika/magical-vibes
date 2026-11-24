package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "BOK", collectorNumber = "36")
public class HeedTheMists extends Card {

    public HeedTheMists() {
        addSpellEffect(new MillEffect(1, Subjects.you()));
        addSpellEffect(new DrawCardEffect(AmountThisWay.milledManaValue()));
    }
}
