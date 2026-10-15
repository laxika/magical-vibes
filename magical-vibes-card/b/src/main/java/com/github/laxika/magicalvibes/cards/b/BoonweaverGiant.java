package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachAurasToSourceEffect;

@CardRegistration(set = "M15", collectorNumber = "5")
public class BoonweaverGiant extends Card {

    public BoonweaverGiant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, AttachAurasToSourceEffect.oneAuraSearch());
    }
}
