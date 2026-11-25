package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ROE", collectorNumber = "66")
public class EnclaveCryptologist extends Card {

    public EnclaveCryptologist() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(PutCountersEffect.onSelf(CounterType.LEVEL)),
                "Level up {1}{U} ({1}{U}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addStaticEffect(new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(3, CounterType.LEVEL)))),
                new GrantActivatedAbilityEffect(lootAbility(), Subjects.source())));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(3, CounterType.LEVEL),
                new GrantActivatedAbilityEffect(drawAbility(), Subjects.source())));
    }

    private static ActivatedAbility lootAbility() {
        return new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(1), new DiscardEffect(1, Subjects.you())),
                "{T}: Draw a card, then discard a card."
        );
    }

    private static ActivatedAbility drawAbility() {
        return new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(1)),
                "{T}: Draw a card."
        );
    }
}
