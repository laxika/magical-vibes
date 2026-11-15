package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * What caused a triggered ability to trigger, as the effects that make an ability trigger an additional time judge it
 * ("if a permanent entering the battlefield causes a triggered ability of a permanent you control to trigger, that
 * ability triggers an additional time", CR 603.2d): a request carries it to the stack door
 * ({@link TriggeredAbilityRequest#withCause}), which counts the additional times there. An ability whose cause no such
 * effect judges carries none.
 */
public sealed interface TriggerCause {

    /** Whether this cause is a creature dying, which the effects that judge dying creatures read (Teysa Karlov). */
    default boolean creatureDying() {
        return false;
    }

    /**
     * A permanent entering the battlefield (Panharmonicon, Elesh Norn, Mother of Machines; CR 603.6a).
     *
     * @param enteringCard         the card the permanent entered as
     * @param enteringControllerId the player it entered under
     * @param enteringPermanentId  the permanent, when the entry knows it, else {@code null}
     */
    record Entered(Card enteringCard, UUID enteringControllerId, UUID enteringPermanentId) implements TriggerCause {
    }

    /** A creature dying (CR 700.4): the creatures that died with it are judged as they last existed (CR 603.10a). */
    record CreatureDied() implements TriggerCause {

        @Override
        public boolean creatureDying() {
            return true;
        }
    }

    /**
     * A permanent, or a player when {@code target} is {@code null}, becoming the target of a spell or ability: "if a
     * creature you control becoming the target of a spell or ability causes a triggered ability of a permanent you
     * control to trigger" (Valiant Emberkin).
     *
     * @param target       the permanent that became the target, or {@code null} for a player
     * @param controllerId its controller
     */
    record BecameTarget(Permanent target, UUID controllerId) implements TriggerCause {
    }

    /**
     * A player casting or copying a spell ("if you casting or copying an instant or sorcery spell causes a triggered
     * ability of a permanent you control to trigger", Veyran, Voice of Duality).
     *
     * @param spell    the spell's card
     * @param playerId the player who cast or copied it
     */
    record SpellCastOrCopied(Card spell, UUID playerId) implements TriggerCause {
    }

    /** A permanent being turned face up (Panoptic Projektor). */
    record TurnedFaceUp() implements TriggerCause {
    }

    /** A player drawing a card (Krang, the All-Powerful). */
    record Drew() implements TriggerCause {
    }

    /**
     * Damage being dealt: combat damage a source dealt to a player ("if a creature you control dealing combat damage
     * to a player causes a triggered ability of a permanent you control to trigger", Felix Five-Boots) and the
     * permanents dealt damage ("if a creature you control being dealt damage causes a triggered ability of a permanent
     * you control to trigger", Wayta, Trainer Prodigy).
     *
     * @param combatDamageToPlayer the combat damage to a player, or {@code null} when the event is none
     * @param damaged              the permanents dealt damage
     */
    record Damage(CombatDamageToPlayer combatDamageToPlayer, List<DamagedPermanent> damaged)
            implements TriggerCause {

        public Damage {
            damaged = List.copyOf(damaged);
        }

        /** Combat damage {@code toPlayer} to a player, no permanent dealt damage. */
        public static Damage toPlayer(CombatDamageToPlayer toPlayer) {
            return new Damage(Objects.requireNonNull(toPlayer, "toPlayer"), List.of());
        }

        /** Damage dealt to {@code permanent}, judged as it is now; its controller is looked up as it's counted. */
        public static Damage toPermanent(Permanent permanent) {
            return new Damage(null, List.of(new DamagedPermanent(permanent, null, null)));
        }

        /** Damage dealt to {@code permanent}, which {@code controllerId} controlled as it was dealt the damage. */
        public static Damage toPermanent(Permanent permanent, UUID controllerId) {
            return new Damage(null, List.of(new DamagedPermanent(permanent, null, controllerId)));
        }

        /** Damage dealt to the permanent {@code permanentId}, looked up as it's counted. */
        public static Damage toPermanentId(UUID permanentId) {
            return new Damage(null, List.of(new DamagedPermanent(null, permanentId, null)));
        }
    }

    /**
     * Combat damage a source dealt to a player.
     *
     * @param sourceCard         the source's card, or {@code null}
     * @param sourcePermanentId  the source's permanent, looked up as the ability is counted, or {@code null}
     * @param sourceControllerId the source's controller as it dealt the damage, or {@code null} for the controller of
     *                           the source permanent as the ability is counted
     * @param creatureSource     whether the source must be a creature: the permanent as it is counted, else its card
     */
    record CombatDamageToPlayer(Card sourceCard, UUID sourcePermanentId, UUID sourceControllerId,
                                boolean creatureSource) {
    }

    /**
     * A permanent dealt damage.
     *
     * @param permanent    the permanent, or {@code null} to look {@code permanentId} up as the ability is counted
     * @param permanentId  the permanent's id when {@code permanent} is {@code null}
     * @param controllerId its controller as it was dealt the damage, or {@code null} for its controller as the ability
     *                     is counted
     */
    record DamagedPermanent(Permanent permanent, UUID permanentId, UUID controllerId) {
    }
}
