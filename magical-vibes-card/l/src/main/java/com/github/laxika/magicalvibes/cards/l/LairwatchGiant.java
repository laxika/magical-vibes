package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "29")
public class LairwatchGiant extends Card {

    public LairwatchGiant() {
        // This creature can block an additional creature each combat.
        addStaticEffect(new GrantAdditionalBlockEffect(1));
        // Whenever this creature blocks two or more creatures, it gains first strike until end of turn.
        addTrigger(Triggers.blocksTwoOrMore(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF));
    }
}
