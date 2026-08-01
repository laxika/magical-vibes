package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;

@CardRegistration(set = "RTR", collectorNumber = "61")
public class DarkRevenant extends Card {

    public DarkRevenant() {
        // Flying is auto-loaded from Scryfall.
        // "When this creature dies, put it on top of its owner's library."
        addEffect(EffectSlot.ON_DEATH, new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0));
    }
}
