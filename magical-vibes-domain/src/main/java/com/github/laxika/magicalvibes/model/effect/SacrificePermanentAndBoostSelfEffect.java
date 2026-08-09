package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Sacrifice one matching permanent, then boost the source permanent until end of turn.
 *
 * <p>The sacrifice and boost resolve synchronously as one effect, which is required for
 * "if you do" wording where the boost is not a reflexive triggered ability.</p>
 */
public record SacrificePermanentAndBoostSelfEffect(
        PermanentPredicate filter,
        int power,
        int toughness,
        String permanentDescription
) implements CardEffect {
}
