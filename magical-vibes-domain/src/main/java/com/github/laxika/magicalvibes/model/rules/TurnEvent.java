package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.TurnStep;

import java.util.Objects;
import java.util.UUID;

/**
 * A moment of the turn's structure that ends the durations of continuous effects: a turn beginning, a step or phase
 * ending, the cleanup step's removal of damage. Each is an occurrence of its own, emitted as the turn reaches it;
 * the duration clock ends the effects that last until it, without the stack. No ability triggers on one; a step or
 * phase beginning is a {@link StepEvent}.
 */
public sealed interface TurnEvent extends RulesEvent {

    /** {@code activePlayerId}'s turn began: the effects that last "until your next turn" of that player end. */
    record Began(UUID activePlayerId) implements TurnEvent {

        public Began {
            Objects.requireNonNull(activePlayerId, "activePlayerId");
        }

        @Override
        public Began deepCopy() {
            return this;
        }
    }

    /**
     * {@code step} of {@code activePlayerId}'s turn ended: the effects that last until the end of that step or phase
     * end (CR 500.5). {@link TurnStep#END_OF_COMBAT} is the combat phase ending, after its end of combat step or
     * because an effect ended combat or the turn.
     */
    record StepEnded(TurnStep step, UUID activePlayerId) implements TurnEvent {

        public StepEnded {
            Objects.requireNonNull(step, "step");
        }

        @Override
        public StepEnded deepCopy() {
            return this;
        }
    }

    /**
     * The cleanup step's second turn-based action in {@code activePlayerId}'s turn: damage is removed and the "until
     * end of turn" and "this turn" effects end, simultaneously and without the stack (CR 514.2).
     */
    record Cleanup(UUID activePlayerId) implements TurnEvent {

        @Override
        public Cleanup deepCopy() {
            return this;
        }
    }
}
