package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "125")
public class BoldwyrAggressor extends Card {

    public BoldwyrAggressor() {
        // Other Giants you control have double strike.
        addStaticEffect(new GrantKeywordEffect(
                Keyword.DOUBLE_STRIKE, Subjects.otherCreaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.GIANT)));
    }
}
