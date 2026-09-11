package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SinnersJudgment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "VOW", collectorNumber = "12")
public class FaithboundJudge extends Card {

    public FaithboundJudge() {
        setBackFaceCard(new SinnersJudgment());

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new NotCondition(new SourceCounterThreshold(3, CounterType.JUDGMENT)),
                new PutCountersOnSelfEffect(CounterType.JUDGMENT)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(3, CounterType.JUDGMENT),
                new CanAttackAsThoughNoDefenderEffect()));

        addCastingOption(new DisturbCast("{5}{W}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "SinnersJudgment";
    }
}
