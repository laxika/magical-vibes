package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/**
 * Creates creature tokens equal to the number of Auras and/or Equipment attached to the source permanent.
 * Generalizes {@link CreateTokenPerEquipmentOnSourceEffect} by allowing counting of Auras, Equipment, or both.
 * Used by Valduk, Keeper of the Flame and similar cards.
 */
public record CreateTokenPerAttachmentOnSourceEffect(
        String tokenName,
        int power,
        int toughness,
        CardColor color,
        List<CardSubtype> subtypes,
        Set<Keyword> keywords,
        Set<CardType> additionalTypes,
        boolean countAuras,
        boolean countEquipment,
        boolean exileAtEndStep
) implements CardEffect {
}
