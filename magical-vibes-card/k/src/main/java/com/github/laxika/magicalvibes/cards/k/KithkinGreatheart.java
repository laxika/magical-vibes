package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "25")
@CardRegistration(set = "MMA", collectorNumber = "21")
public class KithkinGreatheart extends Card {

    public KithkinGreatheart() {
        // As long as you control a Giant, this creature gets +1/+1 and has first strike.
        addStaticEffect(new ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.GIANT)), new StaticBoostEffect(1, 1, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.GIANT)), new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)));
    }
}
