package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;

@CardRegistration(set = "ICE", collectorNumber = "63")
public class Clairvoyance extends Card {

    public Clairvoyance() {
        // "Look at target player's hand."
        addEffect(EffectSlot.SPELL, new LookAtHandEffect());
        // "Draw a card at the beginning of the next turn's upkeep."
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
