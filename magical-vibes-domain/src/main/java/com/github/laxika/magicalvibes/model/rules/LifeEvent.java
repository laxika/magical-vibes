package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;

import java.util.Objects;
import java.util.UUID;

/**
 * A player gained, lost or paid life: the events "whenever you gain life", "whenever an opponent loses life" and
 * "whenever you pay life" abilities trigger on (CR 603.2). Paying life is losing it as well (CR 119.4), and the payment
 * is an event of its own beside the loss. Each is an occurrence of its own, emitted as the life total changes.
 */
public sealed interface LifeEvent extends RulesEvent {

    /** The player whose life total changed. */
    UUID playerId();

    /** How much life the player gained, lost or paid. */
    int amount();

    /**
     * {@code playerId} gained {@code amount} life.
     *
     * @param sourceCard      the card of the spell or ability that caused the gain, or {@code null}
     * @param sourceEntryType what kind of stack object that was, or {@code null}
     */
    record Gained(UUID playerId, int amount, Card sourceCard, StackEntryType sourceEntryType) implements LifeEvent {

        public Gained {
            Objects.requireNonNull(playerId, "playerId");
        }

        @Override
        public Gained deepCopy() {
            return this;
        }
    }

    /** {@code playerId} lost {@code amount} life, by damage or otherwise (CR 120.3a). */
    record Lost(UUID playerId, int amount) implements LifeEvent {

        public Lost {
            Objects.requireNonNull(playerId, "playerId");
        }

        @Override
        public Lost deepCopy() {
            return this;
        }
    }

    /** {@code playerId} paid {@code amount} life (CR 119.4). */
    record Paid(UUID playerId, int amount) implements LifeEvent {

        public Paid {
            Objects.requireNonNull(playerId, "playerId");
        }

        @Override
        public Paid deepCopy() {
            return this;
        }
    }
}
