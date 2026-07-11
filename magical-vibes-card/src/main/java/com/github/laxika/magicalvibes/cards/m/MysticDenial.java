package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "61")
@CardRegistration(set = "P02", collectorNumber = "41")
public class MysticDenial extends Card {

    public MysticDenial() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL, StackEntryType.SORCERY_SPELL)),
                "Target must be a creature or sorcery spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
