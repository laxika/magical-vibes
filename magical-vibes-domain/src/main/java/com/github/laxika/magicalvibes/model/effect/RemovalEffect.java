package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for effects that remove a single targeted permanent from the battlefield
 * by destroying, exiling, or bouncing it. Lets consumers — chiefly the AI evaluators — ask
 * "is this single-target removal, and of what kind" without knowing the concrete effect type,
 * mirroring how {@link ManaProducingEffect} abstracts mana production.
 *
 * <p>Descriptive only: it reports a fact drawn from the record's existing components, never a
 * score.
 */
public interface RemovalEffect extends CardEffect {

    /**
     * The kind of single-target removal this effect performs, or {@code null} when the effect's
     * current configuration is not single-target removal (e.g. a mass/all-matching bounce, which
     * is a board sweep rather than targeted removal). Consumers treat a {@code null} kind as
     * "not single-target removal".
     */
    RemovalKind removalKind();
}
