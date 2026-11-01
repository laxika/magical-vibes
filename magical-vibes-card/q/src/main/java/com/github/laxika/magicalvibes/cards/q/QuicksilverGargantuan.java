package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "39")
public class QuicksilverGargantuan extends Card {

    public QuicksilverGargantuan() {
        addStaticEffect(new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature", 7, 7
        ));
    }
}
