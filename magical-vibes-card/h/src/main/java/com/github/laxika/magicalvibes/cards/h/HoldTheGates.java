package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "GTC", collectorNumber = "16")
public class HoldTheGates extends Card {

    public HoldTheGates() {
        PermanentCount gates = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.GATE), CountScope.CONTROLLER);
        addStaticEffect(new DynamicStaticBoostEffect(
                new Fixed(0), gates, GrantScope.ALL_OWN_CREATURES));
        addStaticEffect(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES));
    }
}
