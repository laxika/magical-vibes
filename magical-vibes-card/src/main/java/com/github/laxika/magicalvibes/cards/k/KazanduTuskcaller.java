package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ROE", collectorNumber = "191")
public class KazanduTuskcaller extends Card {

    public KazanduTuskcaller() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                "Level up {1}{G} ({1}{G}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(2, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(6, CounterType.LEVEL)))),
                new GrantActivatedAbilityEffect(createTokenAbility(1), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(6, CounterType.LEVEL),
                new GrantActivatedAbilityEffect(createTokenAbility(2), GrantScope.SELF)));
    }

    private static ActivatedAbility createTokenAbility(int amount) {
        return new ActivatedAbility(
                true,
                null,
                List.of(new CreateTokenEffect(amount, "Elephant", 3, 3,
                        CardColor.GREEN, List.of(CardSubtype.ELEPHANT), Set.of(), Set.of())),
                amount == 1
                        ? "{T}: Create a 3/3 green Elephant creature token."
                        : "{T}: Create two 3/3 green Elephant creature tokens."
        );
    }
}
