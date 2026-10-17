package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "40")
@CardRegistration(set = "AKR", collectorNumber = "71")
@CardRegistration(set = "C20", collectorNumber = "121")
public class NimbleObstructionist extends Card {

    public NimbleObstructionist() {
        // Flash and Flying come from Scryfall.
        // "When you cycle this card, counter target activated or triggered ability you don't control."
        // A separate trigger from the cycling draw: it goes on the stack above the cycling ability and
        // resolves first. With no legal target it is simply removed from the stack (CR 603.3d), so the
        // cycling draw still happens.
        addCycling("{2}{U}");
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(
                                StackEntryType.ACTIVATED_ABILITY,
                                StackEntryType.TRIGGERED_ABILITY)),
                        new StackEntryNotPredicate(new StackEntryControlledByPredicate()))),
                "Target must be an activated or triggered ability you don't control."))
                .addEffect(EffectSlot.ON_SELF_CYCLED, new CounterSpellEffect());
    }
}
