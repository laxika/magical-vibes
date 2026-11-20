package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M20", collectorNumber = "196")
public class ThicketCrasher extends Card {

    public ThicketCrasher() {
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.otherCreaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL)));
    }
}
