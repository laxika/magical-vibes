package com.github.laxika.magicalvibes.networking.model;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public record CardView(
        String name,
        CardType type,
        List<CardSubtype> subtypes,
        String cardText,
        String manaCost,
        Integer power,
        Integer toughness,
        Set<Keyword> keywords,
        boolean needsTarget,
        boolean needsDamageDistribution,
        boolean hasTapAbility,
        String setCode,
        String collectorNumber,
        String flavorText,
        CardColor color
) {

    public static CardView from(Card card) {
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
                card.isNeedsDamageDistribution(),
                !card.getOnTapEffects().isEmpty(),
                card.getSetCode(),
                card.getCollectorNumber(),
                card.getFlavorText(),
                card.getColor()
        );
    }
}
