package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Damage is dealt, combat or noncombat (CR 120.1, 120.2), or damage that would be dealt is prevented (CR 615.1).
 * Build it through {@link Triggers}. The combat-only forms are {@link CombatTrigger}s.
 *
 * @param event    what happens: who deals the damage, to what, and whether it must be noncombat or excess damage
 * @param relation how the object the event names relates to the ability's source. For a "deals damage" event it is
 *                 the source of the damage ({@link ObjectRelation#THIS} for "this", {@link ObjectRelation#YOU_CONTROL}
 *                 for "a source (creature) you control", {@link ObjectRelation#ENCHANTED} for "enchanted creature",
 *                 {@link ObjectRelation#OPPONENT_CONTROLS} for "a source an opponent controls",
 *                 {@link ObjectRelation#ANY} for any); for an "is dealt damage" event about a permanent it is the
 *                 damaged permanent; for an event about a player being dealt damage it is the source of the damage
 * @param filter   what the permanent the damage collectors judge must be, or {@code null} for any: "whenever a Dragon
 *                 you control is dealt damage" is {@code Triggers.permanentYouControlDealtDamage().withFilter(new
 *                 PermanentHasSubtypePredicate(DRAGON))}. For an "is dealt damage" event and for "this deals damage"
 *                 ({@link DamageEvent#DEALS_DAMAGE} with {@link ObjectRelation#THIS}: "deals damage to one or more
 *                 creatures") it is the damaged permanent; for the other "deals damage" events it is the permanent
 *                 that deals the damage (each of the creatures for "one or more creatures you control deal damage").
 *                 The damage collectors read it from the ability, through {@code DamageAbilityReader}, so
 *                 {@link #watchKey()} drops it
 */
public record DamageTrigger(DamageEvent event, ObjectRelation relation, PermanentPredicate filter) implements Trigger {

    public DamageTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(relation, "relation");
    }

    /** A trigger on any permanent that relates to the source as {@code relation} says. */
    public DamageTrigger(DamageEvent event, ObjectRelation relation) {
        this(event, relation, null);
    }

    /**
     * This trigger narrowed to a permanent that matches {@code filter}, as {@link #filter} says: "whenever a Dinosaur
     * you control is dealt damage", "whenever one or more Pirates you control deal damage to a player".
     */
    public DamageTrigger withFilter(PermanentPredicate filter) {
        Objects.requireNonNull(filter, "filter");
        if (this.filter != null) {
            throw new IllegalStateException("This damage trigger already has a filter: " + this);
        }
        return new DamageTrigger(event, relation, filter);
    }

    /**
     * The damage collectors read the ability itself (its filter, limit and "once for each source"), through
     * {@code DamageAbilityReader}: a damage ability has no legacy view (effect-slot-refactor.md step 14, run 16g).
     */
    @Override
    public boolean collectedAsAbility() {
        return true;
    }

    /** The trigger without its filter, so the {@code DamageWatch} of the unfiltered trigger finds the ability. */
    @Override
    public Trigger watchKey() {
        return filter == null ? this : new DamageTrigger(event, relation);
    }

    /**
     * What a {@link DamageTrigger} triggers on. Unless a value says "noncombat", combat and noncombat damage both
     * count. The values with "one or more" trigger once for each damage event, however many sources it involves.
     */
    public enum DamageEvent {
        /** A source deals damage to anything. */
        DEALS_DAMAGE,
        /**
         * A source deals damage to a player. On an Aura or Equipment, the {@link ObjectRelation#THIS} form is the
         * creature it is attached to.
         */
        DEALS_DAMAGE_TO_PLAYER,
        /**
         * A source deals damage to an opponent of its controller. On an Aura or Equipment, the
         * {@link ObjectRelation#THIS} form is the creature it is attached to.
         */
        DEALS_DAMAGE_TO_OPPONENT,
        /** A permanent deals damage to you. */
        DEALS_DAMAGE_TO_YOU,
        /** A source deals damage to a creature. */
        DEALS_DAMAGE_TO_CREATURE,
        /** A creature deals damage to a planeswalker. */
        DEALS_DAMAGE_TO_PLANESWALKER,
        /** A creature deals damage to the planeswalker the source enchants. */
        DEALS_DAMAGE_TO_ENCHANTED_PLANESWALKER,
        /** A creature deals damage to its controller. */
        DEALS_DAMAGE_TO_ITS_CONTROLLER,
        /** A source deals damage to you or a permanent you control. */
        DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT,
        /** A creature deals damage to you or a permanent you control. */
        CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT,
        /** A source deals noncombat damage to an opponent of its controller. */
        DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT,
        /** A source deals noncombat damage to a creature. */
        DEALS_NONCOMBAT_DAMAGE_TO_CREATURE,
        /** An instant or sorcery spell deals damage. */
        INSTANT_OR_SORCERY_DEALS_DAMAGE,
        /** An instant or sorcery spell deals damage to an opponent or a battle. */
        INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT_OR_BATTLE,
        /** One or more creatures deal damage to a player. */
        ONE_OR_MORE_DEAL_DAMAGE_TO_PLAYER,
        /** One or more creatures deal damage to an opponent. */
        ONE_OR_MORE_DEAL_DAMAGE_TO_OPPONENT,
        /** A permanent is dealt damage. */
        DEALT_DAMAGE,
        /** A permanent is dealt noncombat damage. */
        DEALT_NONCOMBAT_DAMAGE,
        /** A creature is dealt damage. */
        CREATURE_DEALT_DAMAGE,
        /** A creature or planeswalker is dealt excess damage (CR 120.10). */
        CREATURE_OR_PLANESWALKER_DEALT_EXCESS_DAMAGE,
        /** A creature or planeswalker is dealt excess noncombat damage (CR 120.10). */
        CREATURE_OR_PLANESWALKER_DEALT_EXCESS_NONCOMBAT_DAMAGE,
        /** One or more creatures are dealt excess noncombat damage (CR 120.10). */
        ONE_OR_MORE_CREATURES_DEALT_EXCESS_NONCOMBAT_DAMAGE,
        /** You're dealt damage. */
        YOU_DEALT_DAMAGE,
        /** An opponent is dealt damage. */
        OPPONENT_DEALT_DAMAGE,
        /** An opponent is dealt noncombat damage. */
        OPPONENT_DEALT_NONCOMBAT_DAMAGE,
        /** An opponent is dealt noncombat damage by a red instant or sorcery spell or a red planeswalker. */
        OPPONENT_DEALT_NONCOMBAT_DAMAGE_BY_RED_INSTANT_SORCERY_OR_PLANESWALKER,
        /** The player the source enchants is dealt damage. */
        ENCHANTED_PLAYER_DEALT_DAMAGE,
        /** Damage that would be dealt to you is prevented. */
        DAMAGE_TO_YOU_PREVENTED
    }
}
