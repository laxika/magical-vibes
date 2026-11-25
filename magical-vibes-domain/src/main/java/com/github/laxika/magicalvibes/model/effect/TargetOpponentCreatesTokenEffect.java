package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target opponent creates [token]" (Phelddagrif). The token is created under the opponent's
 * control, not the source controller's.
 *
 * <p>This declares no target spec: the ability's opponent target filter declares the player (Phelddagrif), read as
 * {@code targetPlayerDeclaredElsewhere()}. A gift has no target; its opponent is the controller's first other seat
 * until a gift opponent is recorded.
 *
 * @param token blueprint for the token the opponent creates
 * @param gift whether creating the token gives a promised Gift
 */
public record TargetOpponentCreatesTokenEffect(CreateTokenEffect token, boolean gift) implements CardEffect {

    public TargetOpponentCreatesTokenEffect(CreateTokenEffect token) {
        this(token, false);
    }

    public static TargetOpponentCreatesTokenEffect gift(CreateTokenEffect token) {
        return new TargetOpponentCreatesTokenEffect(token, true);
    }
}
