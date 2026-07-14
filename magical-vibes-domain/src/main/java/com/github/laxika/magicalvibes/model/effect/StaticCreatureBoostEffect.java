package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Capability interface for a static P/T (and optional keyword) boost applied to a
 * {@link GrantScope} of creatures — the anthem / equipment / aura shape. Lets consumers — chiefly
 * the AI evaluators/target selector — ask "how much P/T, which keywords, to which scope" without
 * knowing the concrete effect type, mirroring how {@link ManaProducingEffect} abstracts mana
 * production.
 *
 * <p>Descriptive only: it states facts drawn from the record's existing components, never a score.
 */
public interface StaticCreatureBoostEffect extends CardEffect {

    /** The static power boost. */
    int powerBoost();

    /** The static toughness boost. */
    int toughnessBoost();

    /** Keyword(s) granted alongside the P/T boost (may be empty). */
    Set<Keyword> grantedKeywords();

    /** The scope of creatures the boost applies to. */
    GrantScope scope();
}
