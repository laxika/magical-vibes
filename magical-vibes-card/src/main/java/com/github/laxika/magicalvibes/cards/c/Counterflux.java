package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CounterMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;

/**
 * This spell can't be countered.
 * Counter target spell you don't control.
 * <p>
 * Overload {1}{U}{U}{R} (CR 702.96a): paying the overload cost instead of {U}{U}{R} changes
 * "target" to "each", so every spell its controller doesn't control is countered and, per
 * CR 702.96b, the spell chooses no targets at all.
 */
@CardRegistration(set = "RTR", collectorNumber = "153")
public class Counterflux extends Card {

    public Counterflux() {
        StackEntryPredicate spellYouDontControl =
                new StackEntryNotPredicate(new StackEntryControlledByPredicate());

        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{U}{U}{R}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new CounterSpellEffect(),
                new CounterMatchingSpellsEffect(spellYouDontControl)));
        target(new StackEntryPredicateTargetFilter(
                spellYouDontControl,
                "Target must be a spell you don't control"));
    }
}
