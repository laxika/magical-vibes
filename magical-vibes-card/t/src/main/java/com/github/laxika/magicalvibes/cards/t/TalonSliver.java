package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TMP", collectorNumber = "50")
public class TalonSliver extends Card {

    public TalonSliver() {
        // "All Sliver creatures" — every controller's Slivers, including this one.
        addStaticEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE,
                Subjects.allCreatures(),
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
