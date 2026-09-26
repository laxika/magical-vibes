package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "38")
@CardRegistration(set = "SLD", collectorNumber = "1179")
@CardRegistration(set = "SLD", collectorNumber = "2273")
@CardRegistration(set = "SLD", collectorNumber = "2373")
@CardRegistration(set = "SLZ", collectorNumber = "22")
@CardRegistration(set = "SLZ", collectorNumber = "143")
@CardRegistration(set = "SLZ", collectorNumber = "264")
public class MentalMisstep extends Card {

    public MentalMisstep() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryManaValuePredicate(1),
                "Target spell must have mana value 1."
        ))
                .addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
