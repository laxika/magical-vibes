package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "XLN", collectorNumber = "203")
public class RipjawRaptor extends Card {

    public RipjawRaptor() {
        // Enrage — Whenever this creature is dealt damage, draw a card.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new DrawCardEffect());
    }
}
