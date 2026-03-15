package com.github.laxika.magicalvibes.networking.model;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CardView(
        UUID id,
        String name,
        CardType type,
        Set<CardType> additionalTypes,
        Set<CardSupertype> supertypes,
        List<CardSubtype> subtypes,
        String cardText,
        String manaCost,
        Integer power,
        Integer toughness,
        Set<Keyword> keywords,
        boolean hasTapAbility,
        String setCode,
        String collectorNumber,
        CardColor color,
        List<CardColor> colors,
        boolean needsTarget,
        boolean needsSpellTarget,
        List<ActivatedAbilityView> activatedAbilities,
        Integer loyalty,
        boolean hasConvoke,
        boolean hasPhyrexianMana,
        int phyrexianManaCount,
        boolean token,
        String watermark,
        boolean hasAlternateCastingCost,
        int alternateCostLifePayment,
        int alternateCostSacrificeCount,
        List<ActivatedAbilityView> graveyardActivatedAbilities,
        boolean transformable
) {
}
