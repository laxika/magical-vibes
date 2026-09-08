package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * A pending choice to cast one particular card from one opponent's graveyard for free.
 *
 * <p>The graveyard owner identifies the sibling choices that must be removed after one card from
 * that graveyard is accepted.</p>
 */
public record MayCastCardFromOpponentGraveyardWithoutPayingManaCostEffect(UUID graveyardOwnerId)
        implements CardEffect {
}
