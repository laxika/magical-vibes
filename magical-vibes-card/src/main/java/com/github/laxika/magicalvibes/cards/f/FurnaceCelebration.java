package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "SOM", collectorNumber = "90")
public class FurnaceCelebration extends Card {

    public FurnaceCelebration() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, new MayEffect(
                new DealDamageToAnyTargetOnSacrificeEffect(2, 2),
                "Pay {2} to deal 2 damage to any target?"
        ));
    }
}
