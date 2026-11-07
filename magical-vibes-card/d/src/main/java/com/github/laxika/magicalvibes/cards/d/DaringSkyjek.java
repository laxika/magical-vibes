package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "9")
@CardRegistration(set = "GK1", collectorNumber = "79")
public class DaringSkyjek extends Card {

    public DaringSkyjek() {
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(3)),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
    }
}
