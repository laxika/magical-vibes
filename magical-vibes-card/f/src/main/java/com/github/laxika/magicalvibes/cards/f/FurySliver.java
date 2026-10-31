package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TSP", collectorNumber = "157")
@CardRegistration(set = "H09", collectorNumber = "25")
@CardRegistration(set = "SLD", collectorNumber = "638")
@CardRegistration(set = "TSR", collectorNumber = "164")
public class FurySliver extends Card {

    public FurySliver() {
        addStaticEffect(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE,
                GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
