package com.github.laxika.magicalvibes.cards.e;

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
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "66")
public class EnclaveCryptologist extends Card {

    public EnclaveCryptologist() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                "Level up {1}{U} ({1}{U}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(3, CounterType.LEVEL)))),
                new GrantActivatedAbilityEffect(lootAbility(), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(3, CounterType.LEVEL),
                new GrantActivatedAbilityEffect(drawAbility(), GrantScope.SELF)));
    }

    private static ActivatedAbility lootAbility() {
        return new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
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
