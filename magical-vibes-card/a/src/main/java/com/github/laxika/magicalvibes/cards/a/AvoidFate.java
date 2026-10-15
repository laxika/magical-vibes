package com.github.laxika.magicalvibes.cards.a;

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
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSB", collectorNumber = "73")
@CardRegistration(set = "LEG", collectorNumber = "175")
public class AvoidFate extends Card {

    public AvoidFate() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryAnyOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL)),
                                new StackEntryAllOfPredicate(List.of(
                                        new StackEntryTypeInPredicate(Set.of(StackEntryType.ENCHANTMENT_SPELL)),
                                        new StackEntrySubtypeInPredicate(Set.of(CardSubtype.AURA))
                                ))
                        )),
                        new StackEntryTargetsYourPermanentPredicate()
                )),
                "Target must be an instant or Aura spell that targets a permanent you control."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
