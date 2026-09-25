package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCounterToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "56")
public class TenaciousPup extends Card {

    public TenaciousPup() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                RegisterDelayedControllerSpellCastTriggerEffect.oneShotUntilConsumed(
                        new CardTypePredicate(CardType.CREATURE),
                        List.of(
                                new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1),
                                new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                        CounterType.TRAMPLE, 1),
                                new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                        CounterType.VIGILANCE, 1))));
    }
}
