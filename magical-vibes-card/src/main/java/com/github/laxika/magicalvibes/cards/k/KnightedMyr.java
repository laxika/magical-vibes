package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AdaptEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "36")
public class KnightedMyr extends Card {

    public KnightedMyr() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new AdaptEffect(1)),
                "{2}{W}: Adapt 1."
        ));

        // Whenever one or more +1/+1 counters are put on Knighted Myr, it gains double strike until end of turn.
        addEffect(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT,
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF));
    }
}
