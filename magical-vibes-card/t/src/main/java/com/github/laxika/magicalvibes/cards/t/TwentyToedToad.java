package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "16")
@CardRegistration(set = "BLC", collectorNumber = "51")
public class TwentyToedToad extends Card {

    public TwentyToedToad() {
        addEffect(EffectSlot.STATIC, new SetControllerMaximumHandSizeEffect(20));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new MinimumAttackers(2), SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(1))));
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(
                        new AnyOf(List.of(
                                new SourceCounterThreshold(20, CounterType.PLUS_ONE_PLUS_ONE),
                                new CardsInHandAtLeast(20))),
                        new WinGameEffect()));
    }
}
