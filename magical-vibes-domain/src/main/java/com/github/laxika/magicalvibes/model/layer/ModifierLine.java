package com.github.laxika.magicalvibes.model.layer;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * One attributed contribution of a continuous effect to a permanent's displayed
 * characteristics — the per-source breakdown behind the aggregate view fields
 * ({@code PermanentView.powerModifier}, {@code grantedKeywords}, ...). Display-only:
 * produced on demand by {@code GameQueryService.explainStaticBonus} for the client's
 * hover breakdown, never consulted by rules code.
 *
 * @param source            display name of the contributing effect's source (card, emblem,
 *                          or resolved spell)
 * @param power             additive power contribution (layer 7c); 0 when not additive
 * @param toughness         additive toughness contribution (layer 7c); 0 when not additive
 * @param basePower         base power this source SETS (layer 7a/7b); {@code null} when the
 *                          source does not set base power
 * @param baseToughness     base toughness this source sets; {@code null} when unset — base
 *                          lines fold in list order, last non-null component wins (CR 613.7)
 * @param gainedKeywords    keywords granted by this source (layer 6)
 * @param removedKeywords   keywords removed by this source (layer 6)
 * @param losesAllAbilities whether this source removes all abilities (layer 6)
 * @param switchesPt        whether this source switches power and toughness (sublayer 7d);
 *                          two switches cancel, so only the parity of switch lines matters
 */
public record ModifierLine(String source, int power, int toughness,
                           Integer basePower, Integer baseToughness,
                           Set<Keyword> gainedKeywords, Set<Keyword> removedKeywords,
                           boolean losesAllAbilities, boolean switchesPt) {

    public static ModifierLine boost(String source, int power, int toughness) {
        return new ModifierLine(source, power, toughness, null, null, Set.of(), Set.of(), false, false);
    }

    public static ModifierLine abilities(String source, Set<Keyword> gained, Set<Keyword> removed,
                                         boolean losesAllAbilities) {
        return new ModifierLine(source, 0, 0, null, null, Set.copyOf(gained), Set.copyOf(removed),
                losesAllAbilities, false);
    }

    public static ModifierLine base(String source, Integer basePower, Integer baseToughness) {
        return new ModifierLine(source, 0, 0, basePower, baseToughness, Set.of(), Set.of(), false, false);
    }

    public static ModifierLine switchPt(String source) {
        return new ModifierLine(source, 0, 0, null, null, Set.of(), Set.of(), false, true);
    }

    /** True when the line carries nothing displayable (an empty diff — callers should skip it). */
    public boolean isEmpty() {
        return power == 0 && toughness == 0 && basePower == null && baseToughness == null
                && gainedKeywords.isEmpty() && removedKeywords.isEmpty()
                && !losesAllAbilities && !switchesPt;
    }
}
