package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * Marker for a permanent that can't have or gain a keyword while the granting effect applies.
 *
 * <p>This is normally granted with {@link GrantEffectEffect}. Unlike a plain keyword removal,
 * the restriction also prevents later grants from adding the keyword. Resolving keyword grants
 * checks the marker so a one-shot grant that is prevented does not start applying if the
 * restriction later disappears.</p>
 */
public record CantHaveOrGainKeywordEffect(Keyword keyword) implements CardEffect {
}
