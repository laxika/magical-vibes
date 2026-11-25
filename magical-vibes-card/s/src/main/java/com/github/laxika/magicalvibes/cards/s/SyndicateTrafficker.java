package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "101")
public class SyndicateTrafficker extends Card {

    public SyndicateTrafficker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source())
                ),
                "{1}, Sacrifice an artifact: Put a +1/+1 counter on Syndicate Trafficker. It gains indestructible until end of turn."
        ));
    }
}
