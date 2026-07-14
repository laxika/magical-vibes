package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for effects that, when they fire as an
 * {@code ON_COMBAT_DAMAGE_TO_PLAYER} triggered ability in {@code CombatDamageService}, need the
 * created stack entry populated with a particular context — which player the trigger targets and
 * which permanent is its source. Lets the combat engine ask an effect "what trigger context do you
 * need?" instead of matching every concrete effect type by hand.
 *
 * <p>Descriptive only: {@link #combatDamageTriggerContext()} is a pure function of the record's
 * existing components, never a decision or score. A {@code null} result means the effect needs no
 * special context — the trigger fires with the plain (no target, no source) stack entry, exactly as
 * an effect that does not implement this interface. Effects whose bucket membership depends on a
 * component (e.g. a discard/mill/damage effect only qualifies when its recipient is the target
 * player) return the context in that case and {@code null} otherwise.
 */
public interface CombatDamageTriggerContextEffect extends CardEffect {

    /** The stack-entry shape a combat-damage-to-player trigger needs for this effect. */
    enum TriggerContext {
        /**
         * Stack entry carries the combat damage dealt as its {@code xValue} and the damaged player
         * as its {@code targetId}, with no source permanent bound (e.g. Balefire Dragon).
         */
        DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT,

        /**
         * Stack entry binds the damage-dealing creature as its source permanent, with no target
         * player (e.g. Explore / self-counter triggers).
         */
        SOURCE_SELF,

        /**
         * Stack entry binds the damaged player as its {@code targetId} and the damage-dealing
         * creature as its source permanent (e.g. "that player discards a card").
         */
        DAMAGED_PLAYER
    }

    /**
     * The trigger context this effect needs when it fires as a combat-damage-to-player trigger, or
     * {@code null} if it needs the plain stack entry (the default for effects that carry no bound
     * player or source).
     */
    TriggerContext combatDamageTriggerContext();
}
