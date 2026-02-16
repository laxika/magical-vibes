package com.github.laxika.magicalvibes.networking.model;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public record CardView(
        String name,
        CardType type,
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
        boolean needsTarget,
        boolean needsSpellTarget,
        boolean targetsPlayer,
        boolean requiresAttackingTarget,
        List<String> allowedTargetTypes,
        List<ActivatedAbilityView> activatedAbilities,
        Integer loyalty,
        int minTargets,
        int maxTargets,
        boolean hasConvoke
) {
}
