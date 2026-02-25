package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerEquipmentAttachedEffect;

@CardRegistration(set = "SOM", collectorNumber = "92")
public class GoblinGaveleer extends Card {

    public GoblinGaveleer() {
        addEffect(EffectSlot.STATIC, new BoostSelfPerEquipmentAttachedEffect(2, 0));
    }
}