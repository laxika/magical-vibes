package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses a creature or planeswalker to sacrifice. After all chosen permanents are
 * sacrificed simultaneously, each player who sacrificed one may return another permanent card
 * from their graveyard to their hand.
 */
public record EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnAnotherPermanentEffect()
        implements CardEffect {
}
