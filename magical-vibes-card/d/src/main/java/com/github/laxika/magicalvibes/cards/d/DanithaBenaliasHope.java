package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachAurasToSourceEffect;

@CardRegistration(set = "DMU", collectorNumber = "15")
@CardRegistration(set = "DMU", collectorNumber = "287")
public class DanithaBenaliasHope extends Card {

    public DanithaBenaliasHope() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                AttachAurasToSourceEffect.oneAuraOrEquipmentFromHandOrGraveyard());
    }
}
