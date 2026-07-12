package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "179")
public class BloodshotCyclops extends Card {

    public BloodshotCyclops() {
        // {T}, Sacrifice a creature: Bloodshot Cyclops deals damage equal to the sacrificed
        // creature's power to any target. The sacrifice cost snapshots the sacrificed creature's
        // power into the entry's xValue.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeCreatureCost(false, true),
                        new DealDamageToAnyTargetEffect(new XValue())
                ),
                "{T}, Sacrifice a creature: Bloodshot Cyclops deals damage equal to the "
                        + "sacrificed creature's power to any target."
        ));
    }
}
