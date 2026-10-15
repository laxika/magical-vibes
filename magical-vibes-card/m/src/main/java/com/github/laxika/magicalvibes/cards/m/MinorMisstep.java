package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "ONE", collectorNumber = "64")
public class MinorMisstep extends Card {

    public MinorMisstep() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryMaxManaValuePredicate(1),
                "Target spell must have mana value 1 or less."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
