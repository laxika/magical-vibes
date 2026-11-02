package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AdaptEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "36")
@CardRegistration(set = "MH2", collectorNumber = "17")
public class KnightedMyr extends Card {

    public KnightedMyr() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new AdaptEffect(1)),
                "{2}{W}: Adapt 1."
        ));

        // Whenever one or more +1/+1 counters are put on Knighted Myr, it gains double strike until end of turn.
        addTrigger(Triggers.plusOneCountersPutOnThis(),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF));
    }
}
