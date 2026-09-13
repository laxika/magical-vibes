package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DelveCost;

@CardRegistration(set = "FUT", collectorNumber = "91")
@CardRegistration(set = "MMA", collectorNumber = "102")
public class Tombstalker extends Card {

    public Tombstalker() {
        addEffect(EffectSlot.SPELL, new DelveCost());
    }
}
