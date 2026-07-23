package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;

@CardRegistration(set = "5ED", collectorNumber = "76")
@CardRegistration(set = "ICE", collectorNumber = "61")
public class Brainstorm extends Card {

    public Brainstorm() {
        addEffect(EffectSlot.SPELL, new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(3, 2, true));
    }
}
