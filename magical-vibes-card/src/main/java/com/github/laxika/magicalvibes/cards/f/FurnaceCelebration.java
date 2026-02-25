package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "SOM", collectorNumber = "90")
public class FurnaceCelebration extends Card {

    public FurnaceCelebration() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, new MayPayManaEffect(
                "{2}", new DealDamageToAnyTargetEffect(2, false),
                "Pay {2} to deal 2 damage to any target?"
        ));
    }
}
