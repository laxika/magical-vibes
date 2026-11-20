package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M21", collectorNumber = "106")
@CardRegistration(set = "M21", collectorNumber = "358")
public class KaervekTheSpiteful extends Card {

    public KaervekTheSpiteful() {
        addStaticEffect(new StaticBoostEffect(-1, -1, Subjects.otherCreatures()));
    }
}
