package com.github.laxika.magicalvibes.model.trigger;

import java.util.Objects;

/**
 * A player gains life, loses life or pays life (CR 119.3, 119.4). Damage dealt to a player is a loss of life
 * (CR 120.3a), and a player who gains 0 life has no life gain event, so a "whenever [a player] gains life" ability
 * doesn't trigger (CR 119.9). Build it through {@link Triggers}.
 *
 * @param event         what happens to the player's life total
 * @param player        whose life total it is, relative to the ability's controller
 * @param firstEachTurn whether only the player's first such event each turn counts, the events before the source was
 *                      on the battlefield included: "whenever you gain life for the first time each turn" is {@code
 *                      Triggers.youGainLife().firstTimeEachTurn()}. A count of the player's events, not a limit on the
 *                      ability's triggers. The collectors judge it from the ability ({@code LifeAbilityReader}), so
 *                      {@link #watchKey()} drops it
 */
public record LifeTrigger(LifeEvent event, PlayerRelation player, boolean firstEachTurn) implements Trigger {

    public LifeTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(player, "player");
        if (firstEachTurn && event == LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN) {
            throw new IllegalArgumentException("The event already counts the player's first loss: " + event);
        }
    }

    /** A trigger on every such event. */
    public LifeTrigger(LifeEvent event, PlayerRelation player) {
        this(event, player, false);
    }

    /**
     * This trigger narrowed to the player's first such event each turn, as {@link #firstEachTurn} says: "whenever you
     * lose life for the first time each turn".
     */
    public LifeTrigger firstTimeEachTurn() {
        if (firstEachTurn) {
            throw new IllegalStateException("This life trigger already counts its events: " + this);
        }
        return new LifeTrigger(event, player, true);
    }

    /**
     * The trigger the {@code LifeWatch} collectors read this one's abilities by: without {@link #firstEachTurn}, and,
     * for {@link LifeEvent#LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN}, the loss of life of the same player, whose
     * collector judges the rest from the ability ({@code LifeAbilityReader}).
     */
    @Override
    public Trigger watchKey() {
        if (event == LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN) {
            return new LifeTrigger(LifeEvent.LOSES, player);
        }
        return firstEachTurn ? new LifeTrigger(event, player) : this;
    }

    /** What happens to a player's life total. Each value triggers once for each life gain, loss or payment event. */
    public enum LifeEvent {
        /** The player gains one or more life. */
        GAINS,
        /** The player loses one or more life, from damage or otherwise. */
        LOSES,
        /**
         * The player loses one or more life during their own turn, for the first time that turn, counting the losses
         * before the source was on the battlefield: "whenever an opponent loses life for the first time during each
         * of their turns".
         */
        LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN,
        /** The player pays one or more life (CR 119.4). */
        PAYS
    }
}
