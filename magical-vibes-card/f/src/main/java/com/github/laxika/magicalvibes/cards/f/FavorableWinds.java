package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "XLN", collectorNumber = "56")
@CardRegistration(set = "AVR", collectorNumber = "51")
@CardRegistration(set = "GNT", collectorNumber = "21")
@CardRegistration(set = "SCD", collectorNumber = "52")
public class FavorableWinds extends Card {

    public FavorableWinds() {
        // Creatures you control with flying get +1/+1.
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES,
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
