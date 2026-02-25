package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "158")
public class GoldenUrn extends Card {

    public GoldenUrn() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutChargeCounterOnSelfEffect(),
                "Put a charge counter on Golden Urn?"
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new GainLifeEqualToChargeCountersOnSourceEffect()),
                "{T}, Sacrifice Golden Urn: You gain life equal to the number of charge counters on Golden Urn."
        ));
    }
}
