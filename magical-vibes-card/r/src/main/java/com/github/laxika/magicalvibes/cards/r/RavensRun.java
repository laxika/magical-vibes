package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "31")
@CardRegistration(set = "OPCA", collectorNumber = "66")
public class RavensRun extends Card {

    public RavensRun() {
        addStaticEffect(new GrantKeywordEffect(Keyword.WITHER, GrantScope.ALL_CREATURES));

        target(TargetFilters.creature()).addTrigger(Triggers.chaosEnsues(),
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));
        target(TargetFilters.creature()).addTrigger(Triggers.chaosEnsues(),
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 2));
        target(TargetFilters.creature()).addTrigger(Triggers.chaosEnsues(),
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 3));
    }
}
