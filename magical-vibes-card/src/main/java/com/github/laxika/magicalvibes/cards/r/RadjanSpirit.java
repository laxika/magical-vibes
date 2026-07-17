package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "245")
@CardRegistration(set = "5ED", collectorNumber = "320")
public class RadjanSpirit extends Card {

    public RadjanSpirit() {
        // {T}: Target creature loses flying until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new RemoveKeywordEffect(Keyword.FLYING, GrantScope.TARGET)),
                "{T}: Target creature loses flying until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature."
                )
        ));
    }
}
