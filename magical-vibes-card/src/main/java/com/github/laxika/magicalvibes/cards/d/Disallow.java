package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "AER", collectorNumber = "31")
public class Disallow extends Card {

    public Disallow() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryHasTargetPredicate(),
                "Target must be a spell, activated ability, or triggered ability on the stack."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
