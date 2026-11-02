package com.github.laxika.magicalvibes.model.trigger;

import java.util.Objects;

/**
 * A player activates an ability, or a mana ability resolves (CR 605). Build it through {@link Triggers}.
 *
 * @param event  which abilities it triggers on
 * @param player the player who activates the ability
 */
public record AbilityTrigger(AbilityEvent event, PlayerRelation player) implements Trigger {

    public AbilityTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(player, "player");
    }

    /** The activations an {@link AbilityTrigger} triggers on. */
    public enum AbilityEvent {
        /** Any activated ability, mana abilities included: "whenever you activate an ability". */
        ACTIVATED,
        /** An activated ability that isn't a mana ability. */
        NONMANA_ACTIVATED,
        /** An exhaust ability. */
        EXHAUST_ACTIVATED,
        /** An embalm or eternalize ability. */
        ETERNALIZE_OR_EMBALM_ACTIVATED,
        /** A mana ability of this permanent resolves: "whenever a mana ability of this creature resolves". */
        THIS_MANA_ABILITY_RESOLVES
    }
}
