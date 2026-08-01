package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Tap target creature you don't control.
 * <p>
 * Overload {3}{U} (CR 702.96a): paying the overload cost instead of {U} changes "target" to
 * "each", so the spell taps every creature its controller doesn't control and, per CR 702.96b,
 * chooses no targets at all.
 */
@CardRegistration(set = "RTR", collectorNumber = "30")
public class Blustersquall extends Card {

    public Blustersquall() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{U}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new TapPermanentsEffect(TapUntapScope.TARGET),
                new TapPermanentsEffect(TapUntapScope.ALL_CREATURES,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))));
        target(TargetFilters.creatureAnOpponentControls());
    }
}
