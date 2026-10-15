package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "MOR", collectorNumber = "46")
public class ResearchTheDeep extends Card {

    public ResearchTheDeep() {
        // Draw a card, then clash with an opponent. If you win, return Research the Deep to its owner's hand.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addEffect(EffectSlot.SPELL, new ClashEffect(ReturnToHandEffect.selfSpell()));
    }
}
