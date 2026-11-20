package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

/**
 * Assassin's Strike — {4}{B}{B} Sorcery.
 * Destroy target creature. Its controller discards a card.
 */
@CardRegistration(set = "RTR", collectorNumber = "57")
public class AssassinsStrike extends Card {

    public AssassinsStrike() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature."
        ))
                .addSpellEffect(new DestroyTargetPermanentEffect())
                        .addSpellEffect(new DiscardEffect(1, Subjects.controllerOf(Subjects.target())));
    }
}
