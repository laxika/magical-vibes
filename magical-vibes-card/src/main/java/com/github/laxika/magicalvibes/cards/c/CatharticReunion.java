package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "KLD", collectorNumber = "109")
public class CatharticReunion extends Card {

    public CatharticReunion() {
        // As an additional cost to cast this spell, discard two cards.
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null, 2));
        // Draw three cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
