package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

@CardRegistration(set = "FDN", collectorNumber = "98")
public class AmbushWolf extends Card {

    public AmbushWolf() {
        // Flash is loaded from Scryfall.
        // When this creature enters, exile up to one target card from a graveyard.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileCardsFromGraveyardEffect(1, 0));
    }
}
