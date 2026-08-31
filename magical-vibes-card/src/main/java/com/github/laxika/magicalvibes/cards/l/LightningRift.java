package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "ONS", collectorNumber = "217")
public class LightningRift extends Card {

    public LightningRift() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CYCLES, new MayPayManaEffect(
                "{1}",
                new DealDamageToAnyTargetEffect(2),
                "Pay {1} to have Lightning Rift deal 2 damage to any target?"));
    }
}
