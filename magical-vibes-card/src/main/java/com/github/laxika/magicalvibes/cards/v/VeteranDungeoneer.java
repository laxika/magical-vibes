package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;

@CardRegistration(set = "AFR", collectorNumber = "40")
public class VeteranDungeoneer extends Card {

    public VeteranDungeoneer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new VentureIntoDungeonEffect());
    }
}
