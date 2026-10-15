package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;

@CardRegistration(set = "AFR", collectorNumber = "128")
public class YuanTiFangBlade extends Card {

    public YuanTiFangBlade() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new VentureIntoDungeonEffect());
    }
}
