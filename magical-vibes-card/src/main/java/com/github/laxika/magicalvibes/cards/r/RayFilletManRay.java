package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "49")
public class RayFilletManRay extends Card {

    public RayFilletManRay() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofArtifactToken(
                1,
                "Mutagen",
                List.of(),
                List.of(new ActivatedAbility(
                        true,
                        "{1}",
                        List.of(
                                new SacrificeSelfCost(),
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)
                        ),
                        "{1}, {T}, Sacrifice this token: Put a +1/+1 counter on target creature. "
                                + "Activate only as a sorcery.",
                        TargetFilters.creature(),
                        null,
                        null,
                        ActivationTimingRestriction.SORCERY_SPEED
                ))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RemoveCounterFromControlledCreatureCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(1)
                ),
                "{2}, Remove a +1/+1 counter from a creature you control: Draw a card."
        ));
    }
}
