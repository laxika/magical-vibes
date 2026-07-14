package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "113")
public class WindDancer extends Card {

    public WindDancer() {
        // {T}: Target creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{T}: Target creature gains flying until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));
    }
}
