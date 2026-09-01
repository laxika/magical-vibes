package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top cards of the controller's library, grants permission to play them until end of
 * turn, and creates one token for each configured card-type branch represented among the exiled
 * cards.
 *
 * @param count the number of cards to exile, or fewer if the library is short
 * @param landToken the token created when at least one exiled card is a land
 * @param nonlandToken the token created when at least one exiled card is a nonland
 */
public record ExileTopCardsMayPlayThisTurnAndCreateTokensEffect(
        int count,
        CreateTokenEffect landToken,
        CreateTokenEffect nonlandToken
) implements CardEffect {

    public ExileTopCardsMayPlayThisTurnAndCreateTokensEffect {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
        if (landToken == null || nonlandToken == null) {
            throw new IllegalArgumentException("token templates must not be null");
        }
    }

    public ExileTopCardsMayPlayThisTurnAndCreateTokensEffect(
            CreateTokenEffect landToken,
            CreateTokenEffect nonlandToken
    ) {
        this(2, landToken, nonlandToken);
    }
}
