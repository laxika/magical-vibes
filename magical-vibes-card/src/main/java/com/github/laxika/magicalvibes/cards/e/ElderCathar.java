package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSubtypeReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ISD", collectorNumber = "12")
public class ElderCathar extends Card {

    public ElderCathar() {
        // When Elder Cathar dies, put a +1/+1 counter on target creature you control.
        // If that creature is a Human, put two +1/+1 counters on it instead.
        addEffect(EffectSlot.ON_DEATH, new TargetSubtypeReplacementEffect(
                CardSubtype.HUMAN,
                new PutPlusOnePlusOneCounterOnTargetCreatureEffect(1),
                new PutPlusOnePlusOneCounterOnTargetCreatureEffect(2)
        ));
        setTargetFilter(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control"
        ));
    }
}
