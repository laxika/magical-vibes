package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "SCG", collectorNumber = "52")
@CardRegistration(set = "MP2", collectorNumber = "18")
public class Stifle extends Card {

    public Stifle() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.ACTIVATED_ABILITY,
                        StackEntryType.TRIGGERED_ABILITY)),
                "Target must be an activated or triggered ability."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
