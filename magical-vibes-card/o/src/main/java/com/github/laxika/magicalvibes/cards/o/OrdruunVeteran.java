package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "184")
public class OrdruunVeteran extends Card {

    public OrdruunVeteran() {
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(3)),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF));
    }
}
