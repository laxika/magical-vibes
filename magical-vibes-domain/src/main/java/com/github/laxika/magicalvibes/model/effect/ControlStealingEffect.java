package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for effects that gain control of a single targeted permanent for a
 * {@link ControlDuration}. Lets consumers — chiefly the AI evaluators — ask "is this a steal, and
 * for how long" without knowing the concrete effect type, mirroring how {@link ManaProducingEffect}
 * abstracts mana production.
 *
 * <p>Descriptive only: it states a fact drawn from the record's existing components, never a score.
 */
public interface ControlStealingEffect extends CardEffect {

    /** How long control of the stolen permanent is retained. */
    ControlDuration controlDuration();
}
