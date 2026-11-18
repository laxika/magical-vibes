package com.github.laxika.magicalvibes.model.effect;

/**
 * Non-targeted mass version of {@link CombatRequirement#MUST_BLOCK}: every creature the resolving
 * controller's opponents control must be declared as a blocker this turn if it is able to block any
 * attacking creature, including creatures that come under their control later this turn (a floating effect on the
 * resolving controller until end of turn, CR 611.2c). Enforced in
 * {@code CombatBlockService.validateMustBlockIfAbleRequirements}.
 *
 * <p>Used by Predatory Rampage ("Each creature your opponents control blocks this turn if able.").
 */
public record EachOpponentCreatureBlocksThisTurnIfAbleEffect() implements CardEffect {
}
