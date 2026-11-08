package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;

import java.util.Objects;

/**
 * Damage is dealt, combat or noncombat (CR 120.1, 120.2), or damage that would be dealt is prevented (CR 615.1).
 * Build it through {@link Triggers}. The combat-only forms are {@link CombatTrigger}s, but
 * {@link DamageEvent#DEALS_COMBAT_DAMAGE_TO_PLANESWALKER}, which the planeswalker damage collector reads.
 *
 * @param event    what happens: who deals the damage, to what, and whether it must be noncombat or excess damage
 * @param relation how the object the event names relates to the ability's source. For a "deals damage" event it is
 *                 the source of the damage ({@link ObjectRelation#THIS} for "this", {@link ObjectRelation#YOU_CONTROL}
 *                 for "a source (creature) you control", {@link ObjectRelation#ANOTHER_YOU_CONTROL} for "another
 *                 source you control", {@link ObjectRelation#ENCHANTED} for "enchanted creature",
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
 *                 {@link #watchKey()} drops it. On a {@link DamageEvent#DEALS_DAMAGE} or
 *                 {@link DamageEvent#DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER} trigger about another source
 *                 ({@link ObjectRelation#ANY}, {@link ObjectRelation#YOU_CONTROL},
 *                 {@link ObjectRelation#ANOTHER_YOU_CONTROL}) it is the source when that is a
 *                 permanent, and {@link #spellFilter} the source when it isn't
 * @param spellFilter what the source of the damage must be when it isn't a permanent (a spell, or a card dealing
 *                 damage from another zone), or {@code null}: "whenever a red creature or spell deals damage" is
 *                 {@code Triggers.sourceDealsDamage().withFilter(red creature).withSpellFilter(new
 *                 CardColorPredicate(RED))}. Only a {@link DamageEvent#DEALS_DAMAGE},
 *                 {@link DamageEvent#SOURCE_DEALS_DAMAGE_TO_YOU} or
 *                 {@link DamageEvent#DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER} trigger about another source has one. A trigger
 *                 with a filter or a spell filter triggers only for a source of a kind it has a filter for (a
 *                 permanent source that has left the battlefield is judged on its card's characteristics); one with
 *                 neither for every source. {@link #watchKey()} drops it
 * @param damagedFilter what the permanent dealt the damage must be, or {@code null} for any: "whenever this creature
 *                 deals damage to a Vampire" is {@code Triggers.dealsDamageToCreature().withDamagedFilter(new
 *                 PermanentHasSubtypePredicate(VAMPIRE))}. Only a {@link DamageEvent#DEALS_DAMAGE_TO_CREATURE}
 *                 trigger has one, judged on the damaged creature as the damage was dealt, and a
 *                 {@link DamageEvent#CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT} trigger, judged on the damaged
 *                 permanent ("to you or a white creature you control": damage to you always counts), and a
 *                 {@link DamageEvent#DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER} trigger, judged on a permanent dealt the
 *                 damage; {@link #watchKey()} drops it
 * @param damagedPlayerFilter what the player dealt the damage must be, or {@code null}: "whenever damage is dealt to
 *                 one of the chosen players" is {@code Triggers.playerDealtDamage().withDamagedPlayerFilter(new
 *                 PlayerChosenBySourcePredicate())}. Only a {@link DamageEvent#PLAYER_DEALT_DAMAGE} or
 *                 {@link DamageEvent#DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER} trigger has one. On the latter, a trigger
 *                 with a damaged filter or a damaged-player filter triggers only for a permanent or player of a kind it
 *                 has a filter for; one with neither for every permanent and player. {@link #watchKey()} drops it
 * @param combatOnly whether only combat damage counts: "whenever this creature deals combat damage to a creature".
 *                 Only a {@link DamageEvent#DEALS_DAMAGE_TO_CREATURE} trigger has it; {@link #watchKey()} drops it
 */
public record DamageTrigger(DamageEvent event, ObjectRelation relation, PermanentPredicate filter,
                            CardPredicate spellFilter, PermanentPredicate damagedFilter,
                            PlayerPredicate damagedPlayerFilter, boolean combatOnly)
        implements Trigger {

    public DamageTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(relation, "relation");
        if (combatOnly && event != DamageEvent.DEALS_DAMAGE_TO_CREATURE) {
            throw new IllegalArgumentException("Only a deals-damage-to-a-creature trigger is combat only: " + event);
        }
        if (damagedFilter != null && event != DamageEvent.DEALS_DAMAGE_TO_CREATURE
                && event != DamageEvent.CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT
                && event != DamageEvent.DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER) {
            throw new IllegalArgumentException("Only a deals-damage-to-a-creature, a deals-damage-to-you-or-a-"
                    + "permanent-you-control or a deals-damage-to-a-permanent-or-player trigger has a "
                    + "damaged-permanent filter: " + event);
        }
        if (damagedPlayerFilter != null && event != DamageEvent.DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER
                && event != DamageEvent.PLAYER_DEALT_DAMAGE) {
            throw new IllegalArgumentException("Only a player-dealt-damage or a deals-damage-to-a-permanent-or-player "
                    + "trigger has a damaged-player filter: " + event);
        }
        if (event == DamageEvent.PLAYER_DEALT_DAMAGE && (filter != null || relation != ObjectRelation.ANY)) {
            throw new IllegalArgumentException(
                    "A player-dealt-damage trigger names no source: " + relation + " " + filter);
        }
        if (spellFilter != null && (event != DamageEvent.DEALS_DAMAGE && event != DamageEvent.SOURCE_DEALS_DAMAGE_TO_YOU
                && event != DamageEvent.DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER
                || relation != ObjectRelation.ANY && relation != ObjectRelation.YOU_CONTROL
                && relation != ObjectRelation.ANOTHER_YOU_CONTROL)) {
            throw new IllegalArgumentException(
                    "Only a trigger on another source dealing damage has a spell filter: " + event + " " + relation);
        }
    }

    /** A trigger on any permanent that relates to the source as {@code relation} says. */
    public DamageTrigger(DamageEvent event, ObjectRelation relation) {
        this(event, relation, null);
    }

    /** A trigger on a permanent that relates to the source as {@code relation} says and matches {@code filter}. */
    public DamageTrigger(DamageEvent event, ObjectRelation relation, PermanentPredicate filter) {
        this(event, relation, filter, null, null, null, false);
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
        return new DamageTrigger(event, relation, filter, spellFilter, damagedFilter, damagedPlayerFilter, combatOnly);
    }

    /**
     * This {@link DamageEvent#DEALS_DAMAGE} or {@link DamageEvent#SOURCE_DEALS_DAMAGE_TO_YOU} trigger on another
     * source narrowed to a source that isn't a permanent and matches {@code spellFilter}, as {@link #spellFilter}
     * says: "whenever a red creature or spell deals damage".
     */
    public DamageTrigger withSpellFilter(CardPredicate spellFilter) {
        Objects.requireNonNull(spellFilter, "spellFilter");
        if (this.spellFilter != null) {
            throw new IllegalStateException("This damage trigger already has a spell filter: " + this);
        }
        return new DamageTrigger(event, relation, filter, spellFilter, damagedFilter, damagedPlayerFilter, combatOnly);
    }

    /**
     * This {@link DamageEvent#DEALS_DAMAGE_TO_CREATURE} trigger narrowed to a damaged creature that matches
     * {@code damagedFilter}: "whenever this creature deals damage to a Vampire", or this
     * {@link DamageEvent#CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT} trigger to a damaged permanent that matches
     * it ("to you or a white creature you control").
     */
    public DamageTrigger withDamagedFilter(PermanentPredicate damagedFilter) {
        Objects.requireNonNull(damagedFilter, "damagedFilter");
        if (this.damagedFilter != null) {
            throw new IllegalStateException("This damage trigger already has a damaged-creature filter: " + this);
        }
        return new DamageTrigger(event, relation, filter, spellFilter, damagedFilter, damagedPlayerFilter, combatOnly);
    }

    /**
     * This {@link DamageEvent#PLAYER_DEALT_DAMAGE} or {@link DamageEvent#DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER} trigger
     * narrowed to a player dealt the damage who matches {@code damagedPlayerFilter}, as {@link #damagedPlayerFilter}
     * says: "whenever damage is dealt to one of the chosen players".
     */
    public DamageTrigger withDamagedPlayerFilter(PlayerPredicate damagedPlayerFilter) {
        Objects.requireNonNull(damagedPlayerFilter, "damagedPlayerFilter");
        if (this.damagedPlayerFilter != null) {
            throw new IllegalStateException("This damage trigger already has a damaged-player filter: " + this);
        }
        return new DamageTrigger(event, relation, filter, spellFilter, damagedFilter, damagedPlayerFilter, combatOnly);
    }

    /**
     * This {@link DamageEvent#DEALS_DAMAGE_TO_CREATURE} trigger narrowed to combat damage: "whenever this creature
     * deals combat damage to a creature".
     */
    public DamageTrigger withCombatDamageOnly() {
        return new DamageTrigger(event, relation, filter, spellFilter, damagedFilter, damagedPlayerFilter, true);
    }

    /**
     * The trigger without its filters and combat-only narrowing, so the {@code DamageWatch} of the unfiltered trigger
     * finds the ability.
     */
    @Override
    public Trigger watchKey() {
        return filter == null && spellFilter == null && damagedFilter == null && damagedPlayerFilter == null
                && !combatOnly
                ? this : new DamageTrigger(event, relation);
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
        /**
         * A source, a permanent or not, deals damage to you: once for each source (Circle of Affliction's ruling), the
         * damage it dealt to you the event's value. Filtered as {@link #DEALS_DAMAGE} about another source is: the
         * filter is the source as a permanent, the spell filter the source as a spell or card.
         */
        SOURCE_DEALS_DAMAGE_TO_YOU,
        /**
         * A creature deals damage to a creature. The {@link ObjectRelation} is the creature that deals it
         * ({@link ObjectRelation#THIS}, {@link ObjectRelation#EQUIPPED}, {@link ObjectRelation#YOU_CONTROL},
         * {@link ObjectRelation#ANY}), the filter that creature, the damaged-creature filter the creature dealt the
         * damage.
         */
        DEALS_DAMAGE_TO_CREATURE,
        /**
         * A source deals damage to a permanent or player: once for each source and each permanent and player it deals
         * damage to (CR 603.2c; Ghyrson Starn's ruling), that permanent or player the event's recipient and the
         * damage dealt to it the event's value. The relation is the source's; filtered as {@link #DEALS_DAMAGE} about
         * another source is, and on the recipient by the damaged filter (a permanent) and the damaged-player filter
         * (a player).
         */
        DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER,
        /** A creature deals damage to a planeswalker. */
        DEALS_DAMAGE_TO_PLANESWALKER,
        /**
         * A creature deals combat damage to a planeswalker; the planeswalker damage collector reads it with
         * {@link #DEALS_DAMAGE_TO_PLANESWALKER}.
         */
        DEALS_COMBAT_DAMAGE_TO_PLANESWALKER,
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
        /**
         * An instant or sorcery spell that targets only a single creature deals damage to that creature: the creature
         * is the event's permanent and the damage dealt to it the event's value.
         */
        INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_ITS_ONLY_TARGET_CREATURE,
        /**
         * An instant or sorcery spell deals damage to an opponent of its controller: once for each opponent it deals
         * damage to (CR 603.2c), that player being the event's player.
         */
        INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT,
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
        /**
         * Damage is dealt to a player, whoever deals it: once for each player and each damage event, however many
         * sources deal it in that event (CR 603.2c; Boros Reckoner's ruling: "if … dealt damage by multiple sources at
         * once, … its ability triggers once"), that player the event's player and the total damage dealt to them in the
         * event the event's value. Its relation is {@link ObjectRelation#ANY}, it has no filter, and the damaged-player
         * filter narrows the player ("one of the chosen players").
         */
        PLAYER_DEALT_DAMAGE,
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
