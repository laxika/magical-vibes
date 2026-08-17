package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "75")
public class LighthouseChronologist extends Card {

    public LighthouseChronologist() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                "Level up {U} ({U}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(4, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(7, CounterType.LEVEL)))),
                new SetBasePowerToughnessEffect(2, 4, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(7, CounterType.LEVEL),
                new SetBasePowerToughnessEffect(3, 5, GrantScope.SELF)));
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new AllOf(List.of(
                        new NotControllerTurn(),
                        new SourceCounterThreshold(7, CounterType.LEVEL))),
                new ControllerExtraTurnEffect(1)));
    }
}
