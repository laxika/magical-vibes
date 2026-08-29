package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryKickedPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "PLS", collectorNumber = "24")
public class ErtaisTrickery extends Card {

    public ErtaisTrickery() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryKickedPredicate(),
                "Target spell must have been kicked."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
