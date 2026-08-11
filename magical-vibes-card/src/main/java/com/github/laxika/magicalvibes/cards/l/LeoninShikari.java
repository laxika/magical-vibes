package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EquipAbilitiesCanBeActivatedAtInstantSpeedEffect;

@CardRegistration(set = "DST", collectorNumber = "6")
public class LeoninShikari extends Card {

    public LeoninShikari() {
        addEffect(EffectSlot.STATIC, new EquipAbilitiesCanBeActivatedAtInstantSpeedEffect());
    }
}
