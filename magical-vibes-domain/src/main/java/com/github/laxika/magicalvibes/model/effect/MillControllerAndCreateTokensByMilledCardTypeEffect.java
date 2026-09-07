package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Mills one card from the controller's library and creates tokens based on that card's types.
 * The land and creature branches are independent, so a card with both types creates both tokens;
 * the third token is created only when the milled card is neither a creature nor a land.
 */
public record MillControllerAndCreateTokensByMilledCardTypeEffect(
        CreateTokenEffect landToken,
        CreateTokenEffect creatureToken,
        CreateTokenEffect nonCreatureNonLandToken
) implements CardEffect {

    public MillControllerAndCreateTokensByMilledCardTypeEffect {
        Objects.requireNonNull(landToken, "landToken");
        Objects.requireNonNull(creatureToken, "creatureToken");
        Objects.requireNonNull(nonCreatureNonLandToken, "nonCreatureNonLandToken");
    }
}
