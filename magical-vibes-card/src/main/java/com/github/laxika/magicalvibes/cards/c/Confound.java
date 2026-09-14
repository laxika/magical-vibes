package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLS", collectorNumber = "22")
public class Confound extends Card {

    public Confound() {
        target(new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryNotPredicate(new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.ACTIVATED_ABILITY, StackEntryType.TRIGGERED_ABILITY))),
                                new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate()))),
                "Target must be a spell that targets a creature."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect())
          .addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
