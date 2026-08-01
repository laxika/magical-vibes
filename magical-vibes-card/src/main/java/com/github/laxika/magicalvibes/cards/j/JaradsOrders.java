package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandAndCardToGraveyardEffect;

@CardRegistration(set = "RTR", collectorNumber = "175")
public class JaradsOrders extends Card {

    public JaradsOrders() {
        // Search your library for up to two creature cards and reveal them. Put one into your
        // hand and the other into your graveyard. Then shuffle.
        addEffect(EffectSlot.SPELL, SearchLibraryForCardToHandAndCardToGraveyardEffect.upToCreaturesRevealed());
    }
}
