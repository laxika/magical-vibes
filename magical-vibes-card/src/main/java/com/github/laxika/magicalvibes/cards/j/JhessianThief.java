package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "ORI", collectorNumber = "62")
public class JhessianThief extends Card {

    public JhessianThief() {
        // Prowess is loaded from Scryfall keywords.
        // Whenever this creature deals combat damage to a player, draw a card.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(1));
    }
}
