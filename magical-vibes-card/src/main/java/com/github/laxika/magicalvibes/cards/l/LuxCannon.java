package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "173")
public class LuxCannon extends Card {

    public LuxCannon() {
        // {T}: Put a charge counter on Lux Cannon.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutChargeCounterOnSelfEffect()),
                "{T}: Put a charge counter on Lux Cannon."
        ));

        // {T}, Remove three charge counters from Lux Cannon: Destroy target permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveChargeCountersFromSourceCost(3),
                        new DestroyTargetPermanentEffect()
                ),
                "{T}, Remove three charge counters from Lux Cannon: Destroy target permanent."
        ));
    }
}
