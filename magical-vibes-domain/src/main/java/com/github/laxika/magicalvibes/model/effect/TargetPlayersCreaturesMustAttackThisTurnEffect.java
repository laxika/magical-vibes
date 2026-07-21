package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot, player-targeted: every creature the target player controls must attack this turn if
 * able. Sets the transient {@code mustAttackThisTurn} flag on each of that player's creatures,
 * cleared at end of turn via {@code resetModifiers()} and read in
 * {@code CombatAttackService.getMustAttackRequirementCount} (so the requirement only bites a
 * creature that can legally attack). The creatures may attack any legal target — no forced
 * defender, so this is not "attack you". Player-targeted analogue of the non-targeted,
 * predicate-driven {@link MatchingCreaturesMustAttackThisTurnEffect}; used by Imaginary Threats
 * ("Creatures target opponent controls attack this turn if able").
 */
public record TargetPlayersCreaturesMustAttackThisTurnEffect() implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
