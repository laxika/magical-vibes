package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "RNA", collectorNumber = "23")
public class SpiritOfTheSpires extends Card {

    public SpiritOfTheSpires() {
        addStaticEffect(new StaticBoostEffect(0, 1, Subjects.otherCreaturesYouControl(),
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
