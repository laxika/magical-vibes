package com.github.laxika.magicalvibes.model.effect;

/**
 * Non-targeted mass version of {@link MustBlockThisTurnIfAbleEffect}: every creature the resolving
 * controller's opponents control must be declared as a blocker this turn if it is able to block any
 * attacking creature (sets {@code Permanent.mustBlockThisTurnIfAble} on each, cleared at end of
 * turn). Enforced in {@code CombatBlockService.validateMustBlockIfAbleRequirements}.
 *
 * <p>Used by Predatory Rampage ("Each creature your opponents control blocks this turn if able.").
 */
public record EachOpponentCreatureBlocksThisTurnIfAbleEffect() implements CardEffect {
}
