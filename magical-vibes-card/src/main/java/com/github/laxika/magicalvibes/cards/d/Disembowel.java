package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Disembowel — {X}{B} Instant.
 * Destroy target creature with mana value X.
 */
@CardRegistration(set = "RAV", collectorNumber = "85")
public class Disembowel extends Card {

    private static final PermanentPredicate CREATURE_WITH_MANA_VALUE_X = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentManaValueEqualsXPredicate()
    ));

    public Disembowel() {
        target(new PermanentPredicateTargetFilter(
                CREATURE_WITH_MANA_VALUE_X,
                "Target must be a creature with mana value X."
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(CREATURE_WITH_MANA_VALUE_X));
    }
}
