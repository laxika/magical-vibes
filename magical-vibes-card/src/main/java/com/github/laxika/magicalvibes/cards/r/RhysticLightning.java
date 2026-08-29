package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;

@CardRegistration(set = "PCY", collectorNumber = "99")
public class RhysticLightning extends Card {

    public RhysticLightning() {
        addEffect(EffectSlot.SPELL, new MayPayManaEffect(
                "{2}",
                new DealDamageToAnyTargetEffect(2),
                "Pay {2} to reduce the damage to 2?",
                MayPayPayer.TARGET_PLAYER_OR_PERMANENT_CONTROLLER,
                new DealDamageToAnyTargetEffect(4),
                0
        ));
    }
}
