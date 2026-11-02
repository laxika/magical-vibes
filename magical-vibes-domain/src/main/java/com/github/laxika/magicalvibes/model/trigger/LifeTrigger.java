package com.github.laxika.magicalvibes.model.trigger;

import java.util.Objects;

/**
 * A player gains life, loses life or pays life (CR 119.3, 119.4). Damage dealt to a player is a loss of life
 * (CR 120.3a), and a player who gains 0 life has no life gain event, so a "whenever [a player] gains life" ability
 * doesn't trigger (CR 119.9). Build it through {@link Triggers}.
 *
 * @param event  what happens to the player's life total
 * @param player whose life total it is, relative to the ability's controller
 */
public record LifeTrigger(LifeEvent event, PlayerRelation player) implements Trigger {

    public LifeTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(player, "player");
    }

    /** What happens to a player's life total. Each value triggers once for each life gain, loss or payment event. */
    public enum LifeEvent {
        /** The player gains one or more life. */
        GAINS,
        /** The player loses one or more life, from damage or otherwise. */
        LOSES,
        /** The player pays one or more life (CR 119.4). */
        PAYS
    }
}
