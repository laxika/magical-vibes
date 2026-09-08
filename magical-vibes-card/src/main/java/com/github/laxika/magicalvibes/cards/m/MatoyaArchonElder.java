package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "FIN", collectorNumber = "62")
@CardRegistration(set = "FIN", collectorNumber = "439")
public class MatoyaArchonElder extends Card {

    public MatoyaArchonElder() {
        addEffect(EffectSlot.ON_CONTROLLER_SCRIES, new DrawCardEffect(1));
        addEffect(EffectSlot.ON_CONTROLLER_SURVEILS, new DrawCardEffect(1));
    }
}
