package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TSP", collectorNumber = "219")
@CardRegistration(set = "TSR", collectorNumber = "230")
public class SpinneretSliver extends Card {

    public SpinneretSliver() {
        addStaticEffect(new GrantKeywordEffect(Keyword.REACH,
                Subjects.allCreatures(),
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
