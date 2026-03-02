package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockPerEquipmentEffect;

@CardRegistration(set = "MBS", collectorNumber = "9")
public class KembasLegion extends Card {

    public KembasLegion() {
        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockPerEquipmentEffect());
    }
}
