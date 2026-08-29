package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
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
                        new PutCountersOnSourceEffect(1, 1, 1),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)
                ),
                "{1}, Sacrifice an artifact: Put a +1/+1 counter on Syndicate Trafficker. It gains indestructible until end of turn."
        ));
    }
}
