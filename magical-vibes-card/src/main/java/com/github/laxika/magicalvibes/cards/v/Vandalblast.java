package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Destroy target artifact you don't control.
 * <p>
 * Overload {4}{R} (CR 702.96a): paying the overload cost instead of {R} changes "target" to "each",
 * so every artifact its controller doesn't control is destroyed and, per CR 702.96b, the spell
 * chooses no targets at all.
 */
@CardRegistration(set = "RTR", collectorNumber = "111")
public class Vandalblast extends Card {

    public Vandalblast() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{R}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new DestroyTargetPermanentEffect(false),
                new DestroyAllPermanentsEffect(artifactYouDontControl())));
        target(new PermanentPredicateTargetFilter(artifactYouDontControl(),
                "Target must be an artifact you don't control"));
    }

    private static PermanentAllOfPredicate artifactYouDontControl() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
