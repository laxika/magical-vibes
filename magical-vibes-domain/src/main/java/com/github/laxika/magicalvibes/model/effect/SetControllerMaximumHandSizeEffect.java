package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/**
 * Changes the maximum hand size of each player {@code players} names, which is normally seven and is read only by the
 * cleanup step's discard (CR 402.2, 514.1): "your maximum hand size is two" (Recycle), "is increased by one"
 * (Minamo Scrollkeeper), "each opponent's maximum hand size is reduced by one" (Gnat Miser), "the chosen player's
 * maximum hand size is four" (Cursed Rack, {@code chosenPlayer()}), "is equal to the number of hour counters on this"
 * (Midnight Oil, {@code CountersOnSource}), "seven minus the number of those card types" (Winter, inside a
 * {@link ConditionalEffect} on delirium).
 *
 * <p>As a static ability its players are judged against the source and its controller, its amount for the source as
 * it is now; {@code TurnCleanupService.getMaxHandSize} applies every change in timestamp order. Resolved by a spell
 * ("your maximum hand size is reduced by three for the rest of the game", Inspired Idea) it is floated on each named
 * player for the rest of the game with its amount fixed as it resolves; a floated copy applies to the player it is on.
 *
 * @param players who it applies to; the controller by default
 * @param change  whether the amount sets, increases or reduces the running maximum
 * @param amount  the number
 */
public record SetControllerMaximumHandSizeEffect(PlayerSubject players, Change change, DynamicAmount amount)
        implements CardEffect {

    /** How the amount changes the running maximum hand size. */
    public enum Change {
        SET,
        INCREASE,
        REDUCE
    }

    /** "Your maximum hand size is {@code maximumHandSize}." */
    public SetControllerMaximumHandSizeEffect(int maximumHandSize) {
        this(Subjects.you(), Change.SET, new Fixed(maximumHandSize));
    }

    public SetControllerMaximumHandSizeEffect {
        Objects.requireNonNull(players, "players");
        Objects.requireNonNull(change, "change");
        Objects.requireNonNull(amount, "amount");
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("A maximum hand size change takes no target player");
        }
    }

    /** "[Players'] maximum hand size is [amount]." */
    public static SetControllerMaximumHandSizeEffect set(PlayerSubject players, DynamicAmount amount) {
        return new SetControllerMaximumHandSizeEffect(players, Change.SET, amount);
    }

    /** "[Players'] maximum hand size is increased by {@code n}." */
    public static SetControllerMaximumHandSizeEffect increase(PlayerSubject players, int n) {
        return new SetControllerMaximumHandSizeEffect(players, Change.INCREASE, new Fixed(n));
    }

    /** "[Players'] maximum hand size is reduced by {@code n}." */
    public static SetControllerMaximumHandSizeEffect reduce(PlayerSubject players, int n) {
        return new SetControllerMaximumHandSizeEffect(players, Change.REDUCE, new Fixed(n));
    }

    /** This change with its amount fixed at {@code value}, as a resolution floats it (CR 608.2h). */
    public SetControllerMaximumHandSizeEffect fixedAt(int value) {
        return new SetControllerMaximumHandSizeEffect(players, change, new Fixed(value));
    }

    /** The maximum hand size after this change, from {@code currentMax} and the evaluated {@code value}. */
    public int applyTo(int currentMax, int value) {
        return switch (change) {
            case SET -> value;
            case INCREASE -> currentMax + value;
            case REDUCE -> currentMax - value;
        };
    }
}
