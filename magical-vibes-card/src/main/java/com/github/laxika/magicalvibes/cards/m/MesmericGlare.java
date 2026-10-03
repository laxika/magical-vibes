package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

public class MesmericGlare extends Card {

    public MesmericGlare() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryMaxManaValuePredicate(3),
                "Target spell must have mana value 3 or less."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
