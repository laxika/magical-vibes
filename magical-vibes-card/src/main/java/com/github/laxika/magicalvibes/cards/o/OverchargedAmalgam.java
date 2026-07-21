package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExploitEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "INR", collectorNumber = "80")
public class OverchargedAmalgam extends Card {

    public OverchargedAmalgam() {
        // Flash, Flying, Exploit — keywords auto-loaded from Scryfall.

        // Exploit (When this creature enters, you may sacrifice a creature.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExploitEffect(), "Sacrifice a creature?"));

        // When this creature exploits a creature, counter target spell, activated ability, or
        // triggered ability. StackEntryHasTargetPredicate signals that abilities on the stack are
        // legal targets (not just spells).
        target(new StackEntryPredicateTargetFilter(
                new StackEntryHasTargetPredicate(),
                "Target must be a spell, activated ability, or triggered ability on the stack."
        )).addEffect(EffectSlot.ON_EXPLOIT, new CounterSpellEffect());
    }
}
