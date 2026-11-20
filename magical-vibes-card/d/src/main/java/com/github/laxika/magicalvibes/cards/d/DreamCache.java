package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect;

@CardRegistration(set = "6ED", collectorNumber = "66")
@CardRegistration(set = "MIR", collectorNumber = "62")
@CardRegistration(set = "TMP", collectorNumber = "59")
@CardRegistration(set = "C18", collectorNumber = "88")
public class DreamCache extends Card {

    public DreamCache() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect.yours(2, HandToLibraryPlacement.PLAYER_CHOICE));
    }
}
