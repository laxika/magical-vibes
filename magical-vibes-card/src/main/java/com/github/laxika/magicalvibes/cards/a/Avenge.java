package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerWasAttackedByPlayerLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MSC", collectorNumber = "11")
@CardRegistration(set = "MSC", collectorNumber = "295")
public class Avenge extends Card {

    public Avenge() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerWasAttackedByPlayerLastTurn(),
                new ReduceOwnCastCostEffect(new Fixed(2))));
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentIsCreaturePredicate(), new GainLifeEffect(new EventValue())));
    }
}
