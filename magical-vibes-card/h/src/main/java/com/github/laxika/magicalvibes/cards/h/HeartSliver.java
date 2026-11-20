package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TMP", collectorNumber = "182")
@CardRegistration(set = "H09", collectorNumber = "7")
public class HeartSliver extends Card {

    public HeartSliver() {
        // "All Sliver creatures" — every controller's Slivers, including this one.
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE,
                Subjects.allCreatures(),
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
