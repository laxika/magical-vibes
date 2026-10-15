package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "CHK", collectorNumber = "96")
public class Thoughtbind extends Card {

    public Thoughtbind() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryMaxManaValuePredicate(4),
                "Target spell must have mana value 4 or less."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
