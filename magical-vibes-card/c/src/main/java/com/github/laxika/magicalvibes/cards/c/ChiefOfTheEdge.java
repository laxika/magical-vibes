package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "KTK", collectorNumber = "169")
public class ChiefOfTheEdge extends Card {

    public ChiefOfTheEdge() {
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.otherCreaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)));
    }
}
