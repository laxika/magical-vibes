package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CycloneUpkeepEffect;

@CardRegistration(set = "CHR", collectorNumber = "62")
public class Cyclone extends Card {

    public Cyclone() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CycloneUpkeepEffect());
    }
}
