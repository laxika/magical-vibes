package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntrySubtypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "45")
public class Nullify extends Card {

    public Nullify() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAnyOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.ENCHANTMENT_SPELL)),
                                new StackEntrySubtypeInPredicate(Set.of(CardSubtype.AURA))))
                )),
                "Target must be a creature or Aura spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
