package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "216")
public class TrigonOfRage extends Card {

    public TrigonOfRage() {
        // Trigon of Rage enters the battlefield with three charge counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedChargeCountersEffect(3));

        // {R}{R}, {T}: Put a charge counter on Trigon of Rage.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}{R}",
                List.of(new PutChargeCounterOnSelfEffect()),
                "{R}{R}, {T}: Put a charge counter on Trigon of Rage."
        ));

        // {2}, {T}, Remove a charge counter from Trigon of Rage: Target creature gets +3/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new RemoveChargeCountersFromSourceCost(1),
                        new BoostTargetCreatureEffect(3, 0)
                ),
                "{2}, {T}, Remove a charge counter from Trigon of Rage: Target creature gets +3/+0 until end of turn."
        ));
    }
}
