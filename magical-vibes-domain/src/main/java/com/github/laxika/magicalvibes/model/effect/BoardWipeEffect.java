package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for effects that sweep many permanents at once — the "board wipe" shape
 * (mass damage, destroy-all, mass bounce). Lets consumers — chiefly the AI evaluators — ask
 * "does resolving this sweep the board" without knowing the concrete effect type, mirroring how
 * {@link ManaProducingEffect} abstracts mana production.
 *
 * <p>Descriptive only: it reports a fact drawn from the record's existing components, never a
 * score. The board-wipe SCORING stays per-type in the AI (each sweep is valued differently); this
 * interface only answers the uniform recognition question the AI's phase/pressure multipliers ask.
 */
public interface BoardWipeEffect extends CardEffect {

    /**
     * True when this effect's current configuration sweeps many permanents at once (a board wipe).
     * Always-mass effects return {@code true}; a scope-dependent effect (e.g. a bounce that is a
     * board sweep only in its all-matching scope) reports {@code true} only in that configuration.
     */
    boolean sweepsBoard();
}
