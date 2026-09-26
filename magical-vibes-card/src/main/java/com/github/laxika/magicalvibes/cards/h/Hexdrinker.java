package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromEverythingEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1222")
@CardRegistration(set = "MH1", collectorNumber = "168")
public class Hexdrinker extends Card {

    public Hexdrinker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                "Level up {1} ({1}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(3, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(8, CounterType.LEVEL)))),
                new SetBasePowerToughnessEffect(4, 4, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(3, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(8, CounterType.LEVEL)))),
                new GrantEffectEffect(
                        new ProtectionFromCardTypesEffect(Set.of(CardType.INSTANT)), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(8, CounterType.LEVEL),
                new SetBasePowerToughnessEffect(6, 6, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(8, CounterType.LEVEL),
                new GrantEffectEffect(new ProtectionFromEverythingEffect(), GrantScope.SELF)));
    }
}
