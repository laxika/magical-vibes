package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "FRF", collectorNumber = "44")
public class NeutralizingBlast extends Card {

    public NeutralizingBlast() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryIsMulticoloredPredicate(),
                "Target must be a multicolored spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
