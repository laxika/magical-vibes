package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target opponent creates [token]" (Phelddagrif). The token is created under the opponent's
 * control, not the source controller's.
 *
 * <p>Like {@link TargetOpponentMayDrawCardEffect}, this declares no target spec: the engine is
 * two-player, so the opponent is derived from the resolving controller instead of being chosen,
 * which leaves the stack entry's target slot free for a sibling effect on the same ability.
 *
 * @param token blueprint for the token the opponent creates
 */
public record TargetOpponentCreatesTokenEffect(CreateTokenEffect token) implements CardEffect {
}
