package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;

@CardRegistration(set = "PCY", collectorNumber = "73")
public class PlagueFiend extends Card {

    public PlagueFiend() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new MayPayManaEffect(
                "{2}",
                null,
                "Pay {2} to prevent destruction?",
                MayPayPayer.TARGET_PERMANENT_CONTROLLER,
                new DestroyTargetPermanentEffect(),
                0));
    }
}
