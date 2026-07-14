package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "200")
@CardRegistration(set = "7ED", collectorNumber = "200")
@CardRegistration(set = "6ED", collectorNumber = "193")
public class LightningBlast extends Card {

    public LightningBlast() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(4));
    }
}
