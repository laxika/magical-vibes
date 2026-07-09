package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "LRW", collectorNumber = "81")
public class Ringskipper extends Card {

    public Ringskipper() {
        // Flying is auto-loaded from Scryfall.
        // When this creature dies, clash with an opponent. If you win, return this card to its owner's hand.
        addEffect(EffectSlot.ON_DEATH,
                new ClashEffect(new ReturnSourceCardFromGraveyardToOwnerHandEffect()));
    }
}
