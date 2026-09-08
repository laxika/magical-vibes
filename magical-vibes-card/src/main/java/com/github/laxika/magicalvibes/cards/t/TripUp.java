package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;

@CardRegistration(set = "DFT", collectorNumber = "71")
public class TripUp extends Card {

    public TripUp() {
        addEffect(EffectSlot.SPELL, new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(0));
        addCycling("{2}");
    }
}
