package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardTwoUnlessCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "DTK", collectorNumber = "64")
public class MysticMeditation extends Card {

    public MysticMeditation() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new DiscardTwoUnlessCreatureEffect());
    }
}
