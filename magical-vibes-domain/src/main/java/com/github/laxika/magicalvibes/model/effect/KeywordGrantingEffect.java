package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Capability interface for effects that grant keyword(s) to permanents selected by a
 * {@link GrantScope}. Lets consumers — chiefly the AI evaluators/target selector — ask "which
 * keywords, to which scope" without knowing the concrete effect type, mirroring how
 * {@link ManaProducingEffect} abstracts mana production.
 *
 * <p>Descriptive only: it states facts drawn from the record's existing components, never a score.
 */
public interface KeywordGrantingEffect extends CardEffect {

    /** The keyword(s) granted. */
    Set<Keyword> keywords();

    /** The scope of permanents that receive the keyword(s). */
    GrantScope scope();
}
