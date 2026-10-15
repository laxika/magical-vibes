package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;

@CardRegistration(set = "5ED", collectorNumber = "285")
@CardRegistration(set = "CHR", collectorNumber = "61")
@CardRegistration(set = "TSB", collectorNumber = "76")
@CardRegistration(set = "LEG", collectorNumber = "180")
public class CrawGiant extends Card {

    public CrawGiant() {
        // Trample is auto-loaded from Scryfall.
        // Rampage 2: whenever Craw Giant becomes blocked, it gets +2/+2 until end of
        // turn for each creature blocking it beyond the first, i.e. 2 * (blockers - 1).
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new RampageEffect(2));
    }
}
