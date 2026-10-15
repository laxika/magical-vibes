package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

/**
 * Gloomlance — {3}{B}{B} Sorcery.
 * Destroy target creature. If that creature was green or white, its controller discards a card.
 */
@CardRegistration(set = "SHM", collectorNumber = "67")
public class Gloomlance extends Card {

    public Gloomlance() {
        // The discard checks the target's colour, so it runs while the creature is still on the
        // battlefield — before the destroy. Colour doesn't change when it's destroyed, so this is
        // equivalent to the printed "was green or white" wording.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature."
        ))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new TargetPermanentMatches(
                                new PermanentColorInPredicate(Set.of(CardColor.GREEN, CardColor.WHITE))),
                        new DiscardEffect(1, DiscardRecipient.TARGET_PERMANENT_CONTROLLER)))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
