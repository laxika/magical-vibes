package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "215")
public class TrigonOfMending extends Card {

    public TrigonOfMending() {
        // Trigon of Mending enters the battlefield with three charge counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedChargeCountersEffect(3));

        // {W}{W}, {T}: Put a charge counter on Trigon of Mending.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{W}",
                List.of(new PutChargeCounterOnSelfEffect()),
                "{W}{W}, {T}: Put a charge counter on Trigon of Mending."
        ));

        // {2}, {T}, Remove a charge counter from Trigon of Mending: Target player gains 3 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new RemoveChargeCountersFromSourceCost(1),
                        new TargetPlayerGainsLifeEffect(3)
                ),
                "{2}, {T}, Remove a charge counter from Trigon of Mending: Target player gains 3 life."
        ));
    }
}
