package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachAllEquipmentToSourceEffect;

@CardRegistration(set = "MRD", collectorNumber = "110")
public class VulshokBattlemaster extends Card {

    public VulshokBattlemaster() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AttachAllEquipmentToSourceEffect());
    }
}
