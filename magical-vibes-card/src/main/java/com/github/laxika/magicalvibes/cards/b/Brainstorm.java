package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;

@CardRegistration(set = "5ED", collectorNumber = "76")
@CardRegistration(set = "ICE", collectorNumber = "61")
@CardRegistration(set = "MMQ", collectorNumber = "61")
@CardRegistration(set = "BTD", collectorNumber = "3")
@CardRegistration(set = "CST", collectorNumber = "46")
@CardRegistration(set = "CST", collectorNumber = "61")
@CardRegistration(set = "ME2", collectorNumber = "42")
@CardRegistration(set = "DDJ", collectorNumber = "13")
@CardRegistration(set = "VMA", collectorNumber = "58")
public class Brainstorm extends Card {

    public Brainstorm() {
        addEffect(EffectSlot.SPELL, new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(3, 2, HandToLibraryPlacement.TOP));
    }
}
