package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Static effect that multiplies the number of matching tokens created under the controller's control.
 * Used by Parallel Lives (multiplier=2), Doubling Season (token half, multiplier=2), and similar cards.
 * Unlike {@link DoubleDamageEffect} which is global, this only applies to the controller's
 * own token creation — multiple instances on the battlefield stack multiplicatively.
 *
 * @param multiplier the factor by which token creation is multiplied (e.g. 2 for doubling)
 * @param affectedSubtype the token subtype this replacement applies to, or {@code null} for all tokens
 * @param appliesToAllPlayers whether the replacement applies to every player's token creation
 */
public record MultiplyTokenCreationEffect(int multiplier, CardSubtype affectedSubtype, boolean creatureTokensOnly,
                                          boolean appliesToAllPlayers)
        implements TokenCreationReplacementEffect, DoublingEffect {
    public MultiplyTokenCreationEffect(int multiplier, CardSubtype affectedSubtype) {
        this(multiplier, affectedSubtype, false, false);
    }

    public MultiplyTokenCreationEffect(int multiplier, CardSubtype affectedSubtype, boolean creatureTokensOnly) {
        this(multiplier, affectedSubtype, creatureTokensOnly, false);
    }

    public MultiplyTokenCreationEffect(int multiplier, boolean creatureTokensOnly) {
        this(multiplier, null, creatureTokensOnly, false);
    }

    public MultiplyTokenCreationEffect(int multiplier) {
        this(multiplier, null, false, false);
    }

    /** Creates an all-player token multiplier such as Primal Vigor's replacement effect. */
    public static MultiplyTokenCreationEffect forAllPlayers(int multiplier) {
        return new MultiplyTokenCreationEffect(multiplier, null, false, true);
    }

    @Override
    public int replaceTokenCount(int currentCount) {
        return currentCount * multiplier;
    }

    @Override
    public int replacementOrder() {
        return 1;
    }

    @Override
    public boolean isDoublingEffect() {
        return multiplier == 2;
    }
}
