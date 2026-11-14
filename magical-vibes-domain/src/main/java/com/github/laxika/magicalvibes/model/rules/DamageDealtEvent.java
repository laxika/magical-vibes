package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingSourceDamage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Damage was dealt (CR 120.1, 120.2), or damage to a player was prevented (CR 615.1): the events the damage abilities
 * trigger on (CR 603.2). Each is emitted as an occurrence of its own where the damage is dealt; a combat damage step's
 * damage as a whole is a {@link CombatDamageStepEvent}.
 */
public sealed interface DamageDealtEvent extends RulesEvent {

    /**
     * Damage a spell or ability (or a fight) dealt to a permanent.
     *
     * @param target                      the damaged permanent
     * @param damage                      the damage dealt
     * @param entry                       the stack object that dealt it
     * @param sourceControllerId          the source's controller as it dealt the damage
     * @param sourceCard                  the source's card
     * @param sourceId                    the source permanent, or {@code null} for a spell
     * @param dealer                      the source permanent that dealt it, or {@code null}
     * @param reflectionSource            the source as it dealt it, its last-known information once it left (CR 608.2h)
     * @param damagedCreatureControllerId the damaged permanent's controller as it was dealt the damage
     * @param targetControllerId          the damaged permanent's controller the damage was assigned against
     * @param excessDamage                the damage beyond lethal (CR 120.10), or 0
     * @param noncombat                   whether it is noncombat damage
     */
    record ToPermanent(Permanent target, int damage, StackEntry entry, UUID sourceControllerId, Card sourceCard,
                       UUID sourceId, Permanent dealer, Permanent reflectionSource, UUID damagedCreatureControllerId,
                       UUID targetControllerId, int excessDamage, boolean noncombat) implements DamageDealtEvent {

        public ToPermanent {
            Objects.requireNonNull(target, "target");
        }

        @Override
        public ToPermanent deepCopy() {
            return this;
        }
    }

    /**
     * Damage a spell or ability dealt to a player.
     *
     * @param playerId           the damaged player
     * @param damage             the damage dealt
     * @param entry              the stack object that dealt it
     * @param source             the source's card
     * @param sourceControllerId the source's controller as it dealt the damage
     * @param dealtByCreature    the creature the stack object is of that dealt it, or {@code null}
     * @param creatureSource     the source permanent if it is a creature, as it dealt the damage, or {@code null}
     * @param opponentDealer     the permanent or spell as it dealt the damage, for "creatures deal damage to an
     *                           opponent", or {@code null}
     * @param sourcePermanent    the source permanent still on the battlefield, or {@code null}
     * @param dealingPermanent   the source permanent that dealt the damage itself, or {@code null}
     * @param dealingControllerId the controller of {@code dealingPermanent} as it dealt the damage
     */
    record ToPlayer(UUID playerId, int damage, StackEntry entry, Card source, UUID sourceControllerId,
                    Permanent dealtByCreature, Permanent creatureSource, Permanent opponentDealer,
                    Permanent sourcePermanent, Permanent dealingPermanent, UUID dealingControllerId)
            implements DamageDealtEvent {

        public ToPlayer {
            Objects.requireNonNull(playerId, "playerId");
            Objects.requireNonNull(entry, "entry");
        }

        @Override
        public ToPlayer deepCopy() {
            return this;
        }
    }

    /**
     * A resolution finished dealing its noncombat damage: each source that dealt any, keyed as the damage was
     * accumulated (by the source object, or by the stack object for a spell's own damage), with what it dealt to whom,
     * for the abilities that add up a source's damage of one event (CR 603.2c).
     */
    record SourcesDealtDamage(Map<UUID, PendingSourceDamage> sources) implements DamageDealtEvent {

        public SourcesDealtDamage {
            sources = new LinkedHashMap<>(sources);
        }

        @Override
        public SourcesDealtDamage deepCopy() {
            return this;
        }
    }

    /** A creature dealt noncombat damage, for the abilities of the Auras and Equipment attached to it. */
    record DealtByCreature(Permanent creature, int damage) implements DamageDealtEvent {

        @Override
        public DealtByCreature deepCopy() {
            return this;
        }
    }

    /** Combat damage was dealt to a permanent, as the combat damage step's damage is applied. */
    record CombatToPermanent(Permanent permanent, int damage) implements DamageDealtEvent {

        @Override
        public CombatToPermanent deepCopy() {
            return this;
        }
    }

    /**
     * Combat damage was dealt to a player, as the step's damage is applied.
     *
     * @param damage        the damage the player was dealt in all
     * @param damageBySource the damage each source dealt the player, in the order it was dealt
     */
    record CombatToPlayer(UUID playerId, int damage, Map<UUID, Integer> damageBySource) implements DamageDealtEvent {

        public CombatToPlayer {
            damageBySource = new LinkedHashMap<>(damageBySource);
        }

        @Override
        public CombatToPlayer deepCopy() {
            return this;
        }
    }

    /** Combat damage {@code source} dealt to the creature {@code target}, as it is recorded. */
    record CombatToCreature(Permanent source, Permanent target, int damage) implements DamageDealtEvent {

        @Override
        public CombatToCreature deepCopy() {
            return this;
        }
    }

    /** Damage that would have been dealt to {@code playerId} was prevented (CR 615.1). */
    record PreventedToPlayer(UUID playerId, int amount) implements DamageDealtEvent {

        @Override
        public PreventedToPlayer deepCopy() {
            return this;
        }
    }
}
