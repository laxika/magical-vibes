package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ORI", collectorNumber = "155")
public class MagmaticInsight extends Card {

    public MagmaticInsight() {
        // As an additional cost to cast this spell, discard a land card.
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(new CardTypePredicate(CardType.LAND), "land"));
        // Draw two cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
