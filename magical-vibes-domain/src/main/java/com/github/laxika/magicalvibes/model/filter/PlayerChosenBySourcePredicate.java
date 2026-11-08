package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a player the source permanent chose as it entered the battlefield ("one of the chosen players": Sower of
 * Discord's {@code ChoosePlayerOnEnterEffect(2)}), its {@code Permanent.rememberedTargetPlayerIds} read from the source
 * or, once it has left, its last-known information this turn. Matches no player without a source.
 */
public record PlayerChosenBySourcePredicate() implements PlayerPredicate {
}
