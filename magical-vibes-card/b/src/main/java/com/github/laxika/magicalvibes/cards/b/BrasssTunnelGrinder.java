package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TecutlanTheSearingRift;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.DescendedThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;

@CardRegistration(set = "LCI", collectorNumber = "135")
@CardRegistration(set = "LCI", collectorNumber = "373")
public class BrasssTunnelGrinder extends Card {

    public BrasssTunnelGrinder() {
        setBackFaceCard(new TecutlanTheSearingRift());

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DiscardUpToThenDrawThatManyEffect(DiscardUpToThenDrawThatManyEffect.ANY_NUMBER, 1));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new DescendedThisTurn()),
                SequenceEffect.of(
                        PutCountersEffect.onSelf(CounterType.BORE),
                        new ConditionalEffect(new SourceCounterThreshold(3, CounterType.BORE), SequenceEffect.of(
                                RemoveCountersEffect.allFromSelf(CounterType.BORE),
                                new TransformSelfEffect()))));
    }

    @Override
    public String getBackFaceClassName() {
        return "TecutlanTheSearingRift";
    }
}
