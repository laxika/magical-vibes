package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Hearth Kami — {1}{R} Creature — Spirit 2/1.
 * {X}, Sacrifice this creature: Destroy target artifact with mana value X.
 */
@CardRegistration(set = "CHK", collectorNumber = "171")
public class HearthKami extends Card {

    public HearthKami() {
        addActivatedAbility(new ActivatedAbility(false, "{X}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{X}, Sacrifice this creature: Destroy target artifact with mana value X.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentManaValueEqualsXPredicate())),
                        "Target must be an artifact with mana value X.")));
    }
}
