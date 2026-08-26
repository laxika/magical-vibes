package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LCI", collectorNumber = "208")
public class PugnaciousHammerskull extends Card {

    public PugnaciousHammerskull() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new NotCondition(new ControlsAnotherPermanent(
                        new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR))),
                new PutCountersOnSelfEffect(CounterType.STUN)));
    }
}
