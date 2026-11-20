package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MH1", collectorNumber = "18")
public class LancerSliver extends Card {

    public LancerSliver() {
        addStaticEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE,
                Subjects.creaturesYouControl(), new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
