package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "5")
public class BantBattlemage extends Card {

    public BantBattlemage() {
        // {G}, {T}: Target creature gains trample until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, "{G}",
                List.of(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                "{G}, {T}: Target creature gains trample until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));

        // {U}, {T}: Target creature gains flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, "{U}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{U}, {T}: Target creature gains flying until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));
    }
}
