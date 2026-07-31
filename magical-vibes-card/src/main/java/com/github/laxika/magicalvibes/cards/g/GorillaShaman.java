package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Gorilla Shaman — {R} Creature — Ape Shaman 1/1.
 * {X}{X}{1}: Destroy target noncreature artifact with mana value X.
 */
@CardRegistration(set = "ALL", collectorNumber = "72a")
@CardRegistration(set = "ALL", collectorNumber = "72b")
public class GorillaShaman extends Card {

    public GorillaShaman() {
        // Two {X} symbols: the paid X is doubled for the mana cost, and the same X bounds
        // the target's mana value.
        addActivatedAbility(new ActivatedAbility(false, "{X}{X}{1}",
                List.of(new DestroyTargetPermanentEffect()),
                "{X}{X}{1}: Destroy target noncreature artifact with mana value X.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                                new PermanentManaValueEqualsXPredicate())),
                        "Target must be a noncreature artifact with mana value X.")));
    }
}
