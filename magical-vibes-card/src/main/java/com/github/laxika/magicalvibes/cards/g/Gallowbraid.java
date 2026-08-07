package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;

@CardRegistration(set = "WTH", collectorNumber = "70")
public class Gallowbraid extends Card {

    public Gallowbraid() {
        // Cumulative upkeep—Pay 1 life. (Trample is auto-loaded from Scryfall.)
        addEffect(EffectSlot.UPKEEP_TRIGGERED, CumulativeUpkeepEffect.life(1));
    }
}
