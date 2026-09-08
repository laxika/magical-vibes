package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;

@CardRegistration(set = "ULG", collectorNumber = "33")
public class Intervene extends Card {

    public Intervene() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate()),
                "Target spell must target a creature."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
