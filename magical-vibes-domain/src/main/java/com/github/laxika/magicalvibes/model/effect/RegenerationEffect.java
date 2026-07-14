package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker interface for effects that regenerate a creature (a shield against the next destruction).
 * Lets consumers — chiefly the AI evaluators/target selector — recognise a regeneration effect
 * without naming the concrete type, mirroring how {@link ManaProducingEffect} marks mana
 * production.
 *
 * <p>Descriptive only: membership is a fact about the effect, never a score.
 */
public interface RegenerationEffect extends CardEffect {
}
