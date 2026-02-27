package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "196")
public class RatchetBomb extends Card {

    public RatchetBomb() {
        // {T}: Put a charge counter on Ratchet Bomb.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutChargeCounterOnSelfEffect()),
                "{T}: Put a charge counter on Ratchet Bomb."
        ));

        // {T}, Sacrifice Ratchet Bomb: Destroy each nonland permanent with mana value equal to
        // the number of charge counters on Ratchet Bomb.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(),
                        new DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect()),
                "{T}, Sacrifice Ratchet Bomb: Destroy each nonland permanent with mana value equal to the number of charge counters on Ratchet Bomb."
        ));
    }
}
