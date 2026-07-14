package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureGainToughnessLosePowerToHandEffect;

@CardRegistration(set = "EVE", collectorNumber = "128")
public class SaplingOfColfenor extends Card {

    public SaplingOfColfenor() {
        // Whenever this creature attacks, reveal the top card of your library. If it's a creature
        // card, you gain life equal to that card's toughness, lose life equal to its power, then
        // put it into your hand. (Indestructible is auto-loaded from Scryfall.)
        addEffect(EffectSlot.ON_ATTACK, new RevealTopCardCreatureGainToughnessLosePowerToHandEffect());
    }
}
