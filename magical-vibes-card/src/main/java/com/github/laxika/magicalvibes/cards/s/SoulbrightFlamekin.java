package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SoulbrightFlamekinEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "190")
public class SoulbrightFlamekin extends Card {

    public SoulbrightFlamekin() {
        // {2}: Target creature gains trample until end of turn. If this is the third time this
        // ability has resolved this turn, you may add {R}{R}{R}{R}{R}{R}{R}{R}.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET),
                        new SoulbrightFlamekinEffect()
                ),
                "{2}: Target creature gains trample until end of turn. If this is the third time this ability has resolved this turn, you may add {R}{R}{R}{R}{R}{R}{R}{R}.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
