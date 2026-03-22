package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardUnlessAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "XLN", collectorNumber = "48")
public class ChartACourse extends Card {

    public ChartACourse() {
        // Draw two cards. Then discard a card unless you attacked this turn.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new DiscardCardUnlessAttackedThisTurnEffect());
    }
}
