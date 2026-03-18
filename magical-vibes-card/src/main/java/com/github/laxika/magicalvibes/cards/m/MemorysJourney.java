package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;

@CardRegistration(set = "ISD", collectorNumber = "66")
public class MemorysJourney extends Card {

    public MemorysJourney() {
        addEffect(EffectSlot.SPELL, new ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, 3));
        addCastingOption(new FlashbackCast("{G}"));
    }
}
