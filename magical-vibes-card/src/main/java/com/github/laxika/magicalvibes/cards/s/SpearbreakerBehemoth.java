package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "150")
public class SpearbreakerBehemoth extends Card {

    public SpearbreakerBehemoth() {
        // Indestructible is auto-loaded from Scryfall.
        // {1}: Target creature with power 5 or greater gains indestructible until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}",
                List.of(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET)),
                "{1}: Target creature with power 5 or greater gains indestructible until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtLeastPredicate(5)
                        )),
                        "Target must be a creature with power 5 or greater"
                )
        ));
    }
}
