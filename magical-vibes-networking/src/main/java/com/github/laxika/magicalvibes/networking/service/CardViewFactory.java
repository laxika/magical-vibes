package com.github.laxika.magicalvibes.networking.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.networking.model.CardView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CardViewFactory {

    public CardView create(Card card) {
        return new CardView(
                card.getName(),
                card.getType(),
                card.getSupertypes(),
                card.getSubtypes(),
                card.getCardText(),
                card.getManaCost(),
                card.getPower(),
                card.getToughness(),
                card.getKeywords(),
                card.isNeedsTarget(),
                !card.getEffects(EffectSlot.ON_TAP).isEmpty() || !card.getEffects(EffectSlot.TAP_ACTIVATED_ABILITY).isEmpty(),
                !card.getEffects(EffectSlot.MANA_ACTIVATED_ABILITY).isEmpty(),
                card.getSetCode(),
                card.getCollectorNumber(),
                card.getFlavorText(),
                card.getColor(),
                computeAllowedTargetTypes(card),
                computeTargetsPlayer(card)
        );
    }

    private boolean computeTargetsPlayer(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.MANA_ACTIVATED_ABILITY)) {
            if (effect instanceof MillTargetPlayerEffect) {
                return true;
            }
        }
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof MillTargetPlayerEffect) {
                return true;
            }
        }
        return false;
    }

    private List<CardType> computeAllowedTargetTypes(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.TAP_ACTIVATED_ABILITY)) {
            if (effect instanceof TapTargetPermanentEffect tapEffect) {
                return new ArrayList<>(tapEffect.allowedTypes());
            }
        }
        return List.of();
    }
}
