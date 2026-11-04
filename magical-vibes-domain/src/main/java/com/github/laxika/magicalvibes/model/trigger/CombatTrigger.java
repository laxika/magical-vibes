package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Something happens in combat: creatures attack (CR 508.1), block (CR 509.1) or become blocked (CR 509.1h), an
 * attacking creature isn't blocked, or combat damage is dealt (CR 510.2). Build it through {@link Triggers}.
 *
 * @param event    what happens
 * @param relation how the creature that attacks, blocks, becomes blocked or deals or is dealt the damage relates to the
 *                 ability's source: {@link ObjectRelation#THIS} for "this creature", {@link ObjectRelation#YOU_CONTROL}
 *                 for "a creature you control", {@link ObjectRelation#ANY} for "a creature",
 *                 {@link ObjectRelation#EQUIPPED} or {@link ObjectRelation#ENCHANTED} for the creature the source is
 *                 attached to, {@link ObjectRelation#OPPONENT_CONTROLS} for "a creature an opponent controls"
 * @param filter   what the creature must be, or {@code null} for any creature: "whenever a Dragon you control attacks"
 *                 is {@code Triggers.creatureYouControlAttacks().withFilter(new PermanentHasSubtypePredicate(DRAGON))}.
 *                 For "this creature blocks" and "this creature becomes blocked" it is the creature on the other side
 *                 of the block, "blocks a creature with flying", "becomes blocked by a white creature" (CR 509.3f);
 *                 for every other event it is the creature that attacks, blocks, becomes blocked or deals the
 *                 damage, as the engine's combat collectors judge it. "A creature with the same name as this" and "one
 *                 or more goaded creatures" are filters their own collectors read
 *                 ({@link Triggers#creatureWithSameNameDealsCombatDamageToPlayer()},
 *                 {@link Triggers#goadedCreaturesDealCombatDamageToOpponent()}); any other filter the collectors read
 *                 from the ability ({@code CombatAbilityReader}), so {@link #watchKey()} drops it
 */
public record CombatTrigger(CombatEvent event, ObjectRelation relation, PermanentPredicate filter) implements Trigger {

    public CombatTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(relation, "relation");
    }

    /** A trigger on any creature that relates to the source as {@code relation} says. */
    public CombatTrigger(CombatEvent event, ObjectRelation relation) {
        this(event, relation, null);
    }

    /**
     * This trigger narrowed to a creature that matches {@code filter}, as {@link #filter} says: "whenever a Dragon you
     * control attacks", "whenever this creature becomes blocked by a white creature".
     */
    public CombatTrigger withFilter(PermanentPredicate filter) {
        Objects.requireNonNull(filter, "filter");
        if (this.filter != null) {
            throw new IllegalStateException("This combat trigger already has a filter: " + this);
        }
        return new CombatTrigger(event, relation, filter);
    }

    /**
     * The trigger without the filter the combat collectors read from the ability, so the {@code CombatWatch} of the
     * unfiltered trigger finds the ability. The same-name and goaded filters name collectors of their own and stay.
     */
    @Override
    public Trigger watchKey() {
        if (filter == null || equals(Triggers.creatureWithSameNameDealsCombatDamageToPlayer())
                || equals(Triggers.goadedCreaturesDealCombatDamageToOpponent())) {
            return this;
        }
        return new CombatTrigger(event, relation);
    }

    /**
     * The attack, block and combat damage collectors read the ability itself (its filter, limit and multiplicity),
     * through {@code CombatAbilityReader}: a combat ability has no legacy view (effect-slot-refactor.md step 14, run
     * 16f).
     */
    @Override
    public boolean collectedAsAbility() {
        return true;
    }

    /**
     * What happens in combat that a {@link CombatTrigger} triggers on. The values without "one or more" trigger once
     * for each creature; those with it once for each declaration or combat damage step, however many creatures it
     * involves.
     */
    public enum CombatEvent {
        /**
         * A creature is declared as an attacker: "whenever this creature attacks". On an Aura or Equipment, the
         * {@link ObjectRelation#THIS} form is "whenever enchanted (equipped) creature attacks", or the ability the
         * Aura or Equipment grants that creature.
         */
        ATTACKS,
        /** A creature attacks you or a planeswalker you control. */
        ATTACKS_YOU_OR_YOUR_PLANESWALKER,
        /** A creature attacks you. */
        ATTACKS_YOU,
        /**
         * A creature attacks while you're the defending player: it attacks you, a planeswalker you control or a battle
         * you protect.
         */
        ATTACKS_WHILE_YOU_DEFEND,
        /** One or more creatures attack: "whenever you attack", "whenever a player attacks". */
        ONE_OR_MORE_ATTACK,
        /** One or more creatures attack a player: "whenever you attack a player", once for each player attacked. */
        ONE_OR_MORE_ATTACK_A_PLAYER,
        /** One or more creatures attack a player or planeswalker, once for each one attacked. */
        ONE_OR_MORE_ATTACK_A_PLAYER_OR_PLANESWALKER,
        /** One or more creatures attack you. */
        ONE_OR_MORE_ATTACK_YOU,
        /** An opponent attacks a planeswalker you control with one or more creatures, once for each planeswalker. */
        ONE_OR_MORE_ATTACK_YOUR_PLANESWALKER,
        /**
         * An attacking creature causes a triggered ability of its own to trigger, or this creature enlists a creature
         * (Aradesh, the Founder; Benalish Knight-Counselor).
         */
        ATTACKING_CREATURE_ABILITY_TRIGGERS,
        /** A creature attacks and isn't blocked. */
        ATTACKS_UNBLOCKED,
        /** One or more creatures attack you and aren't blocked. */
        ONE_OR_MORE_ATTACK_YOU_UNBLOCKED,
        /** A creature blocks. On an Aura or Equipment, the {@link ObjectRelation#THIS} form is the attached creature. */
        BLOCKS,
        /** This creature blocks two or more creatures. */
        BLOCKS_TWO_OR_MORE,
        /** One or more creatures block. */
        ONE_OR_MORE_BLOCK,
        /**
         * A creature becomes blocked. On an Aura or Equipment, the {@link ObjectRelation#THIS} form is the attached
         * creature.
         */
        BECOMES_BLOCKED,
        /** One or more creatures become blocked. */
        ONE_OR_MORE_BECOME_BLOCKED,
        /** A creature deals combat damage. */
        COMBAT_DAMAGE,
        /**
         * A creature deals combat damage to a player. On an Aura or Equipment, the {@link ObjectRelation#THIS} form is
         * the attached creature; on a Curse, a creature dealing combat damage to the enchanted player.
         */
        COMBAT_DAMAGE_TO_PLAYER,
        /** A creature deals combat damage to one of your opponents. */
        COMBAT_DAMAGE_TO_OPPONENT,
        /** One or more creatures deal combat damage to one of your opponents. */
        ONE_OR_MORE_COMBAT_DAMAGE_TO_OPPONENT,
        /** A creature deals combat damage to its owner. */
        COMBAT_DAMAGE_TO_OWNER,
        /** A creature deals combat damage to a player or planeswalker. */
        COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER,
        /** A creature deals combat damage to a player or battle. */
        COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE,
        /** A creature deals combat damage to a creature. */
        COMBAT_DAMAGE_TO_CREATURE,
        /** A creature deals combat damage to one or more blocking creatures. */
        COMBAT_DAMAGE_TO_BLOCKING_CREATURE,
        /** A creature deals combat damage to you. */
        COMBAT_DAMAGE_TO_YOU,
        /** A permanent is dealt combat damage. */
        DEALT_COMBAT_DAMAGE,
        /** Combat damage is dealt to you. */
        YOU_DEALT_COMBAT_DAMAGE,
        /** Combat damage is dealt to you or a planeswalker you control. */
        YOU_OR_YOUR_PLANESWALKER_DEALT_COMBAT_DAMAGE
    }
}
