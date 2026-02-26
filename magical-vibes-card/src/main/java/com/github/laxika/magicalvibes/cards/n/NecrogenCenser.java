package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "184")
public class NecrogenCenser extends Card {

    public NecrogenCenser() {
        // This artifact enters with two charge counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedChargeCountersEffect(2));

        // {T}, Remove a charge counter from Necrogen Censer: Target player loses 2 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveChargeCountersFromSourceCost(1),
                        new TargetPlayerLosesLifeAndControllerGainsLifeEffect(2, 0)
                ),
                "{T}, Remove a charge counter from Necrogen Censer: Target player loses 2 life."
        ));
    }
}
