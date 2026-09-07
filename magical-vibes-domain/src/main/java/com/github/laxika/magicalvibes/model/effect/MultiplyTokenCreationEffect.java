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
 */
public record MultiplyTokenCreationEffect(int multiplier, CardSubtype affectedSubtype, boolean creatureTokensOnly)
        implements TokenCreationReplacementEffect {
        public MultiplyTokenCreationEffect(int multiplier, CardSubtype affectedSubtype) {
            this(multiplier, affectedSubtype, false);
        }


    public MultiplyTokenCreationEffect(int multiplier, boolean creatureTokensOnly) {
        this(multiplier, null, creatureTokensOnly);
    }

    public MultiplyTokenCreationEffect(int multiplier) {
        this(multiplier, null, false);
    }

    @Override
    public int replaceTokenCount(int currentCount) {
        return currentCount * multiplier;
    }

    @Override
    public int replacementOrder() {
        return 1;
    }
}
