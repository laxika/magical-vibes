package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "168")
public class CrimsonRoc extends Card {

    public CrimsonRoc() {
        // Whenever this creature blocks a creature without flying,
        // this creature gets +1/+0 and gains first strike until end of turn.
        PermanentNotPredicate withoutFlying = new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING));
        addTrigger(Triggers.blocks().withFilter(withoutFlying), new BoostSelfEffect(1, 0), new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF));
    }
}
