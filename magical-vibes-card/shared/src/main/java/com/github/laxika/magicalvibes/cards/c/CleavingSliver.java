package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MH1", collectorNumber = "121")
public class CleavingSliver extends Card {

    public CleavingSliver() {
        addStaticEffect(new StaticBoostEffect(2, 0, Subjects.creaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
