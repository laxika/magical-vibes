package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "73")
public class HarmattanEfreet extends Card {

    public HarmattanEfreet() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{1}{U}{U}: Target creature gains flying until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )));
    }
}
