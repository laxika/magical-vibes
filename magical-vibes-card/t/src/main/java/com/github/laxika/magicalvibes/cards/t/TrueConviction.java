package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "SOM", collectorNumber = "25")
@CardRegistration(set = "SLD", collectorNumber = "1082")
@CardRegistration(set = "SS2", collectorNumber = "6")
@CardRegistration(set = "C14", collectorNumber = "93")
@CardRegistration(set = "SCD", collectorNumber = "38")
public class TrueConviction extends Card {

    public TrueConviction() {
        addStaticEffect(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.ALL_OWN_CREATURES));
        addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.ALL_OWN_CREATURES));
    }
}
