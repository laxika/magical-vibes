package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Return target nonland permanent you don't control to its owner's hand.
 * <p>
 * Overload {6}{U} (CR 702.96a): paying the overload cost instead of {1}{U} changes "target" to
 * "each", so every nonland permanent its controller doesn't control is returned and, per
 * CR 702.96b, the spell chooses no targets at all.
 */
@CardRegistration(set = "RTR", collectorNumber = "35")
public class CyclonicRift extends Card {

    public CyclonicRift() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{6}{U}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                ReturnToHandEffect.target(),
                ReturnToHandEffect.allPermanentsMatching(nonlandYouDontControl())));
        target(new PermanentPredicateTargetFilter(nonlandYouDontControl(),
                "Target must be a nonland permanent you don't control"));
    }

    private static PermanentAllOfPredicate nonlandYouDontControl() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
