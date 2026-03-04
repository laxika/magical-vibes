package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "130")
public class Shriekhorn extends Card {

    public Shriekhorn() {
        // Shriekhorn enters the battlefield with three charge counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedChargeCountersEffect(3));

        // {T}, Remove a charge counter from Shriekhorn: Target player mills two cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveChargeCountersFromSourceCost(1),
                        new MillTargetPlayerEffect(2)
                ),
                "{T}, Remove a charge counter from Shriekhorn: Target player mills two cards."
        ));
    }
}
