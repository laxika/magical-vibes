package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/**
 * Creates one creature token for each creature put into the controller's graveyard
 * from the battlefield this turn. Used by Fresh Meat (NPH #109).
 */
public record CreateTokensPerOwnCreatureDeathsThisTurnEffect(
        String tokenName,
        int power,
        int toughness,
        CardColor color,
        List<CardSubtype> subtypes,
        Set<Keyword> keywords,
        Set<CardType> additionalTypes
) implements CardEffect {

    /** Convenience constructor for tokens with no keywords or additional types */
    public CreateTokensPerOwnCreatureDeathsThisTurnEffect(String tokenName, int power, int toughness,
                                                           CardColor color, List<CardSubtype> subtypes) {
        this(tokenName, power, toughness, color, subtypes, Set.of(), Set.of());
    }
}
