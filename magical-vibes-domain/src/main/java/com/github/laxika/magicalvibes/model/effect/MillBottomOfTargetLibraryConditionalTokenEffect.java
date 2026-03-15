package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * Target player puts the bottom card of their library into their graveyard.
 * If that card matches the specified type, the controller creates a creature token.
 *
 * <p>Used by Cellar Door and similar "mill from bottom" cards with conditional token creation.
 *
 * @param conditionType the card type that triggers token creation (e.g. CREATURE)
 * @param tokenName     the name of the token to create
 * @param tokenPower    the power of the token
 * @param tokenToughness the toughness of the token
 * @param tokenColor    the color of the token
 * @param tokenSubtypes the subtypes of the token
 */
public record MillBottomOfTargetLibraryConditionalTokenEffect(
        CardType conditionType,
        String tokenName,
        int tokenPower,
        int tokenToughness,
        CardColor tokenColor,
        List<CardSubtype> tokenSubtypes
) implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
