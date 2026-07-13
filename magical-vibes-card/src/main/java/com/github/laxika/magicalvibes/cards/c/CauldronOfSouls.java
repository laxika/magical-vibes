package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "248")
public class CauldronOfSouls extends Card {

    public CauldronOfSouls() {
        // {T}: Choose any number of target creatures. Each of those creatures gains persist until end of turn.
        // GrantScope.TARGET applies the keyword grant to every creature in the target group; the persist
        // return is handled by PermanentRemovalService via the granted Keyword.PERSIST.
        TargetFilter creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature");
        addActivatedAbility(new ActivatedAbility(
                true,  // requires tap
                null,  // no mana cost
                List.of(new GrantKeywordEffect(Keyword.PERSIST, GrantScope.TARGET)),
                "{T}: Choose any number of target creatures. Each of those creatures gains persist until end of turn.",
                creatureFilter,          // enforced on every chosen target position
                null, null, null,        // loyaltyCost, maxActivationsPerTurn, timingRestriction
                List.of(creatureFilter), // non-empty to enable multi-target selection
                0,                       // minTargets — "any number" includes zero
                99                       // maxTargets
        ));
    }
}
