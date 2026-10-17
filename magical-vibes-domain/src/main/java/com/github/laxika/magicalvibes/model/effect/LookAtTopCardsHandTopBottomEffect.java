package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top {@code count} cards of the controller's library: one goes into hand, one goes on
 * top of the library (or into the graveyard when {@code topToGraveyard} is set, which is a plain
 * zone change and not milling) and the rest go on the bottom.
 */
public record LookAtTopCardsHandTopBottomEffect(int count, boolean topToGraveyard) implements CardEffect {

    public LookAtTopCardsHandTopBottomEffect(int count) {
        this(count, false);
    }
}
