package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.networking.model.CardView;
import org.springframework.stereotype.Service;

@Service
public class CardViewFactory {

    public CardView create(Card card) {
        return new CardView(
                card.getName(),
                card.getType(),
                card.getSubtypes(),
                card.getCardText(),
                card.getManaCost(),
                card.getPower(),
                card.getToughness(),
                card.getKeywords(),
                card.isNeedsTarget(),
                !card.getEffects(EffectSlot.ON_TAP).isEmpty(),
                !card.getEffects(EffectSlot.MANA_ACTIVATED_ABILITY).isEmpty(),
                card.getSetCode(),
                card.getCollectorNumber(),
                card.getFlavorText(),
                card.getColor()
        );
    }
}
