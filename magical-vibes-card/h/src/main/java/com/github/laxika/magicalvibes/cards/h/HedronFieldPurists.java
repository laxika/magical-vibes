package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamagePerSourceToControllerAndCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "25")
public class HedronFieldPurists extends Card {

    public HedronFieldPurists() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                "Level up {2}{W} ({2}{W}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(5, CounterType.LEVEL)))),
                new PreventFixedDamagePerSourceToControllerAndCreaturesEffect(1)));
        addEffect(EffectSlot.STATIC, preventionAtLevel(5, 2));
    }

    private ConditionalEffect preventionAtLevel(int level, int amount) {
        return new ConditionalEffect(new SourceCounterThreshold(level, CounterType.LEVEL),
                new PreventFixedDamagePerSourceToControllerAndCreaturesEffect(amount));
    }
}
