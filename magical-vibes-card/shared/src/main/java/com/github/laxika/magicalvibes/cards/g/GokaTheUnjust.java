package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Flipped face of {@link com.github.laxika.magicalvibes.cards.i.InitiateOfBlood}.
 */
public class GokaTheUnjust extends Card {

    public GokaTheUnjust() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToTargetCreatureEffect(4)),
                "{T}: Goka the Unjust deals 4 damage to target creature that was dealt damage this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentDealtDamageThisTurnPredicate()
                        )),
                        "Target must be a creature that was dealt damage this turn"
                )
        ));
    }
}
