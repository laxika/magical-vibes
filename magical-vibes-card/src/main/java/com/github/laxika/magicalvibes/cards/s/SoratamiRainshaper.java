package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "89")
public class SoratamiRainshaper extends Card {

    public SoratamiRainshaper() {
        // {3}, Return a land you control to its owner's hand: Target creature you control gains
        // shroud until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new ReturnMultiplePermanentsToHandCost(1, new PermanentIsLandPredicate()),
                        new GrantKeywordEffect(Keyword.SHROUD, GrantScope.TARGET)),
                "{3}, Return a land you control to its owner's hand: Target creature you control gains shroud until end of turn.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                )));
    }
}
