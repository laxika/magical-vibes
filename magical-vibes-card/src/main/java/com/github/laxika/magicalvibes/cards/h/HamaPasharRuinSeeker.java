package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalDungeonRoomTriggerEffect;

@CardRegistration(set = "AFR", collectorNumber = "224")
public class HamaPasharRuinSeeker extends Card {

    public HamaPasharRuinSeeker() {
        addEffect(EffectSlot.STATIC, new AdditionalDungeonRoomTriggerEffect());
    }
}
