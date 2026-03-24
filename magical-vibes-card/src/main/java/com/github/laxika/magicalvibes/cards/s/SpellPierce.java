package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "81")
public class SpellPierce extends Card {

    public SpellPierce() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))
                ),
                "Target must be a noncreature spell."
        ))
                .addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(2));
    }
}
