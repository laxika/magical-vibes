package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target opponent creates [token]" (Phelddagrif). The token is created under the opponent's
 * control, not the source controller's.
 *
 * <p>This declares no target spec: the ability's opponent target filter declares the player (Phelddagrif), read as
 * {@code targetPlayerDeclaredElsewhere()}. A gift has no target; its opponent is the controller's first other seat
 * until a gift opponent is recorded.
 *
 * <p>"An opponent creates [token]" chosen as the spell resolves (Ingenious Mastery) names that player as
 * {@code creator}: the opponent a {@link ChoosePlayerEffect#opponent()} step chose ({@code playersThisWay(CHOOSE)}).
 *
 * @param token blueprint for the token the opponent creates
 * @param gift whether creating the token gives a promised Gift
 * @param creator the player who creates the tokens, or {@code null} for the targeted opponent (a gift: the opponent)
 */
public record TargetOpponentCreatesTokenEffect(CreateTokenEffect token, boolean gift, PlayerSubject creator)
        implements CardEffect {

    public TargetOpponentCreatesTokenEffect(CreateTokenEffect token) {
        this(token, false, null);
    }

    public static TargetOpponentCreatesTokenEffect gift(CreateTokenEffect token) {
        return new TargetOpponentCreatesTokenEffect(token, true, null);
    }

    /** "[creator] creates [token]": {@code creator} is a player an earlier step chose. */
    public static TargetOpponentCreatesTokenEffect createdBy(PlayerSubject creator, CreateTokenEffect token) {
        return new TargetOpponentCreatesTokenEffect(token, false, creator);
    }
}
