package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;

@CardRegistration(set = "AFR", collectorNumber = "31")
public class PlanarAlly extends Card {

    public PlanarAlly() {
        addEffect(EffectSlot.ON_ATTACK, new VentureIntoDungeonEffect());
    }
}
