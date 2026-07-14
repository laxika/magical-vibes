package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;

@CardRegistration(set = "6ED", collectorNumber = "66")
public class DreamCache extends Card {

    public DreamCache() {
        addEffect(EffectSlot.SPELL, new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(3, 2));
    }
}
