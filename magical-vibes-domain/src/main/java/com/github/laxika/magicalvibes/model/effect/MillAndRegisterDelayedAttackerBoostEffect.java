package com.github.laxika.magicalvibes.model.effect;

/**
 * Mill {@code millCount} cards from the controller's library, then register a delayed triggered
 * ability for the rest of the turn: whenever a creature attacks, it gets
 * +{@code powerPerCreature}/+{@code toughnessPerCreature} until end of turn for each creature
 * card put into the controller's graveyard this way.
 *
 * <p>Used by Song of Blood ({@code millCount=4}, {@code powerPerCreature=1},
 * {@code toughnessPerCreature=0}).
 */
public record MillAndRegisterDelayedAttackerBoostEffect(
        int millCount, int powerPerCreature, int toughnessPerCreature) implements CardEffect {
}
