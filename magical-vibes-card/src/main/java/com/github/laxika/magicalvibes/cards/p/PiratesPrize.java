package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "XLN", collectorNumber = "68")
public class PiratesPrize extends Card {

    public PiratesPrize() {
        // Draw two cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));

        // Create a Treasure token.
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofTreasureToken(1));
    }
}
