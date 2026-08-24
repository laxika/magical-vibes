package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the controller's library. A land creates the supplied token; otherwise
 * the controller may cast the exiled card until the end of their next turn.
 */
public record ExileTopCardCreateTokenIfLandOrMayCastUntilNextTurnEffect(
        CreateTokenEffect landToken
) implements CardEffect {
}
