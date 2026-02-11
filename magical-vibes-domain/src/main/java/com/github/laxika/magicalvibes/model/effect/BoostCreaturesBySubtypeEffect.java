package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

public record BoostCreaturesBySubtypeEffect(
        Set<CardSubtype> affectedSubtypes,
        int powerBoost,
        int toughnessBoost,
        Set<Keyword> grantedKeywords
) implements CardEffect {
}
