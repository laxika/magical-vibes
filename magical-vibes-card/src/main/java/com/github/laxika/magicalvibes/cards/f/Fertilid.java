package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSearchesLibraryForBasicLandToBattlefieldTappedEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "122")
public class Fertilid extends Card {

    public Fertilid() {
        // This creature enters with two +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));

        // {1}{G}, Remove a +1/+1 counter from this creature: Target player searches their library
        // for a basic land card, puts it onto the battlefield tapped, then shuffles.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new TargetPlayerSearchesLibraryForBasicLandToBattlefieldTappedEffect()
                ),
                "{1}{G}, Remove a +1/+1 counter from Fertilid: Target player searches their library "
                        + "for a basic land card, puts it onto the battlefield tapped, then shuffles."
        ));
    }
}
