package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.CombatDamageState;

import java.util.Objects;
import java.util.UUID;

/**
 * A combat damage step's damage was dealt (CR 510.2): the event the combat-damage abilities and the damage abilities
 * that see combat damage trigger on (CR 603.2). The step's damage is one event, matched in two stages of its own: as it
 * is dealt, before the state-based actions (the abilities that wait on the stack while those run are held in
 * {@code state}), and after them.
 *
 * @param stage             which part of the step's abilities this is
 * @param state             the step's damage: what each source dealt to which player, planeswalker, battle and creature
 * @param activePlayerId    the attacking player
 * @param defendingPlayerId the defending player
 */
public record CombatDamageStepEvent(Stage stage, CombatDamageState state, UUID activePlayerId,
                                    UUID defendingPlayerId) implements RulesEvent {

    /** The parts of a combat damage step's abilities, in the order they are matched. */
    public enum Stage {
        /** As the damage is dealt, before the state-based actions. */
        DEALT,
        /** After the state-based actions the damage caused. */
        AFTER_STATE_BASED_ACTIONS
    }

    public CombatDamageStepEvent {
        Objects.requireNonNull(stage, "stage");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(activePlayerId, "activePlayerId");
    }

    /** This event itself: it is matched as it is emitted, and the step's state is the engine's working state. */
    @Override
    public CombatDamageStepEvent deepCopy() {
        return this;
    }
}
