package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Sacrifice one matching permanent, then boost the source permanent and grant temporary keywords.
 *
 * <p>The sacrifice and boost resolve synchronously as one effect, which is required for
 * "if you do" wording where the boost is not a reflexive triggered ability.</p>
 *
 * @param grantedKeywords keywords granted to the source until end of turn
 */
public record SacrificePermanentAndBoostSelfEffect(
        PermanentPredicate filter,
        int power,
        int toughness,
        String permanentDescription,
        Set<Keyword> grantedKeywords
) implements CardEffect {

    public SacrificePermanentAndBoostSelfEffect(PermanentPredicate filter, int power, int toughness,
                                                String permanentDescription) {
        this(filter, power, toughness, permanentDescription, Set.of());
    }

    public SacrificePermanentAndBoostSelfEffect {
        grantedKeywords = Set.copyOf(grantedKeywords);
    }
}
