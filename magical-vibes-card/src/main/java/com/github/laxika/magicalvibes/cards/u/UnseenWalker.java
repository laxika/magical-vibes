package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "262")
public class UnseenWalker extends Card {

    public UnseenWalker() {
        // {1}{G}{G}: Target creature gains forestwalk until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{G}{G}",
                List.of(new GrantKeywordEffect(Keyword.FORESTWALK, GrantScope.TARGET)),
                "{1}{G}{G}: Target creature gains forestwalk until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));
    }
}
