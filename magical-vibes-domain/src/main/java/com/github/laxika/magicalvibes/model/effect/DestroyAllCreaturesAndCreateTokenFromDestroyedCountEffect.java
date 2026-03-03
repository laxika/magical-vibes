package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;
import java.util.Set;

/**
 * Destroys all creatures, then creates a single creature token whose power and toughness
 * equal the number of creatures actually destroyed this way (i.e. not counting indestructible
 * or regenerated creatures). The token is always colorless.
 *
 * <p>Used by Phyrexian Rebirth.
 */
public record DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect(
        String tokenName,
        List<CardSubtype> tokenSubtypes,
        Set<CardType> tokenAdditionalTypes
) implements CardEffect {
}
