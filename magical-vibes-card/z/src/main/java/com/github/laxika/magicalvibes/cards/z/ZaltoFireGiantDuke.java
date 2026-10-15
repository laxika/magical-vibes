package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;

@CardRegistration(set = "AFR", collectorNumber = "171")
public class ZaltoFireGiantDuke extends Card {

    public ZaltoFireGiantDuke() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new VentureIntoDungeonEffect());
    }
}
