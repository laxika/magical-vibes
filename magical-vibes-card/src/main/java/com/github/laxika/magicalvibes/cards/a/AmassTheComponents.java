package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;

@CardRegistration(set = "AVR", collectorNumber = "43")
public class AmassTheComponents extends Card {

    public AmassTheComponents() {
        addEffect(EffectSlot.SPELL, new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(
                3, 1, HandToLibraryPlacement.BOTTOM));
    }
}
