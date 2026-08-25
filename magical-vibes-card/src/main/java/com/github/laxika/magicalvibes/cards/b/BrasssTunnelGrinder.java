package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TecutlanTheSearingRift;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DescendedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenTransformIfThresholdEffect;

@CardRegistration(set = "LCI", collectorNumber = "135")
public class BrasssTunnelGrinder extends Card {

    public BrasssTunnelGrinder() {
        setBackFaceCard(new TecutlanTheSearingRift());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DiscardUpToThenDrawThatManyEffect(DiscardUpToThenDrawThatManyEffect.ANY_NUMBER, 1));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new DescendedThisTurn(),
                        new PutCounterOnSelfThenTransformIfThresholdEffect(CounterType.BORE, 3)));
    }

    @Override
    public String getBackFaceClassName() {
        return "TecutlanTheSearingRift";
    }
}
