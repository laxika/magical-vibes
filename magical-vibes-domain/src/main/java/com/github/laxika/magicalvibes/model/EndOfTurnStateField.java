package com.github.laxika.magicalvibes.model;

/**
 * One "until end of turn" / "this turn" store that the cleanup step clears (CR 514.2), tracked so
 * that under Runed Terror's sequential phases the part of it created while one player was active
 * can outlive another player's cleanup step (see {@link EndOfTurnStateShare}). Most fields live on a
 * {@link Permanent} ({@link Permanent#captureEndOfTurnState()}); the {@code PLAYER_} fields live in
 * {@link GameData}'s per-player prevention shield maps.
 */
public enum EndOfTurnStateField {

    POWER_MODIFIER(Kind.ADDITIVE),
    TOUGHNESS_MODIFIER(Kind.ADDITIVE),
    ADDITIONAL_BLOCKS(Kind.ADDITIVE),
    BASE_POWER_TOUGHNESS_OVERRIDDEN(Kind.FLAG),
    BASE_POWER_OVERRIDE(Kind.VALUE),
    BASE_TOUGHNESS_OVERRIDE(Kind.VALUE),
    CANT_BE_BLOCKED(Kind.FLAG),
    CANT_BE_BLOCKED_GRANTED_ABILITY(Kind.FLAG),
    CANT_ATTACK_THIS_TURN(Kind.FLAG),
    CANT_BLOCK_THIS_TURN(Kind.FLAG),
    CANT_BLOCK_GRANTED_ABILITY_THIS_TURN(Kind.FLAG),
    MUST_BLOCK_THIS_TURN_IF_ABLE(Kind.FLAG),
    MUST_BLOCK_EACH_ATTACKING_CREATURE_THIS_TURN_IF_ABLE(Kind.FLAG),
    MUST_ATTACK_THIS_TURN(Kind.FLAG),
    MUST_ATTACK_TARGET_ID(Kind.VALUE),
    MUST_BE_BLOCKED_THIS_TURN(Kind.FLAG),
    MUST_BE_BLOCKED_BY_ALL_THIS_TURN(Kind.FLAG),
    BLOCKED_WITHOUT_BLOCKERS(Kind.FLAG),
    AURA_EFFECTS_IGNORED_BY_PLAYERS(Kind.SET),
    DAMPING_ENGINE_IGNORED_BY_PLAYERS(Kind.SET),
    CANT_REGENERATE_THIS_TURN(Kind.FLAG),
    DAMAGE_CANT_BE_PREVENTED_OR_REDIRECTED(Kind.FLAG),
    DAMAGED_CREATURES_CANT_REGENERATE(Kind.FLAG),
    EXILE_DAMAGED_CREATURES_INSTEAD_OF_DYING(Kind.FLAG),
    EXILE_INSTEAD_OF_DIE(Kind.FLAG),
    EXILE_INSTEAD_OF_DIE_SOURCE_PERMANENT_ID(Kind.VALUE),
    DAMAGE_TO_OPPONENT_CREATURE_BOUNCE(Kind.FLAG),
    TEMPORARY_TRIGGERED_EFFECTS(Kind.SLOT_LISTS),
    SADDLED(Kind.FLAG),
    ANIMATED_UNTIL_END_OF_TURN(Kind.FLAG),
    ANIMATED_POWER(Kind.VALUE),
    ANIMATED_TOUGHNESS(Kind.VALUE),
    ANIMATED_COLOR(Kind.VALUE),
    GRANTED_KEYWORDS(Kind.SET),
    REMOVED_KEYWORDS(Kind.SET),
    TRANSIENT_COLORS(Kind.SET),
    COLOR_OVERRIDDEN(Kind.FLAG),
    TRANSIENT_SUBTYPES(Kind.LIST),
    TRANSIENT_LAND_TYPE_OVERRIDE(Kind.VALUE),
    TRANSIENT_CREATURE_TYPE_OVERRIDE(Kind.VALUE),
    TRANSIENT_CREATURE_TYPE_OVERRIDES(Kind.LIST),
    GRANTED_CARD_TYPES(Kind.SET),
    PROTECTION_FROM_CARD_TYPES(Kind.SET),
    PROTECTION_FROM_COLORS(Kind.SET),
    PROTECTION_FROM_PLAYERS(Kind.SET),
    PROTECTION_FROM_COLORLESS(Kind.FLAG),
    PROTECTION_FROM_NON_SUBTYPE_CREATURES(Kind.SET),
    PROTECTION_FROM_OPPONENT_CREATURES(Kind.FLAG),
    PROTECTION_REMOVED(Kind.FLAG),
    BLOCK_RESTRICTIONS(Kind.LIST),
    UNBLOCKABLE_IF_DEFENDER_CONTROLS(Kind.LIST),
    EXILE_IF_LEAVES_BATTLEFIELD(Kind.FLAG),
    SHROUD_IGNORED_BY_PLAYERS(Kind.SET),
    CANT_BLOCK_IDS(Kind.SET),
    MUST_BLOCK_IDS(Kind.SET),
    LOSES_ALL_ABILITIES(Kind.FLAG),
    SUPPRESSED_STATIC_EFFECT_TYPES(Kind.SET),
    SUPPRESSED_STATIC_EFFECT_INSTANCES(Kind.IDENTITY_LIST),
    LOSES_ALL_CREATURE_TYPES(Kind.FLAG),
    TRANSIENT_REMOVED_SUBTYPES(Kind.SET),
    TEMPORARY_ACTIVATED_ABILITIES(Kind.LIST),
    ADAPT_OVERRIDES(Kind.SHIELD),
    TEXT_REPLACEMENTS(Kind.LIST),
    DAMAGE_PREVENTION_SHIELD(Kind.SHIELD),
    DAMAGE_TO_COUNTER_PREVENTION_SHIELD(Kind.SHIELD),
    DAMAGE_TO_PLUS_ONE_PLUS_ONE_COUNTER_PREVENTION_SHIELD(Kind.SHIELD),
    ALL_DAMAGE_TO_PLUS_ONE_PLUS_ONE_COUNTER_PREVENTION_SHIELD(Kind.FLAG),
    DAMAGE_DESTRUCTION_SHIELD(Kind.SHIELD),
    LAND_DESTRUCTION_SHIELD(Kind.SHIELD),
    REGENERATION_SHIELD(Kind.SHIELD),
    MINUS_ONE_COUNTER_REGENERATION_SHIELD(Kind.SHIELD),
    PLAYER_DAMAGE_PREVENTION_SHIELD(Kind.SHIELD),
    PLAYER_COMBAT_DAMAGE_PREVENTION_SHIELD(Kind.SHIELD);

    /** How a field's value is split between players and recombined. */
    public enum Kind {
        /** A {@code boolean}: credited to the player active when it became true. */
        FLAG,
        /** An {@code int} sum of contributions (P/T modifiers): each player keeps their own delta. */
        ADDITIVE,
        /** An {@code int} count that is used up (prevention/regeneration shields): each player keeps their
         *  delta, never more than what is left. */
        SHIELD,
        /** A single value that the latest effect overwrites (base P/T, chosen type): kept by the player
         *  active when it was last changed. */
        VALUE,
        /** A set: each element is credited to the player active when it was added. */
        SET,
        /** A list compared by {@code equals}: each added element is credited to the active player. */
        LIST,
        /** A list compared by identity (CR 611.2c effect instances). */
        IDENTITY_LIST,
        /** An {@link EffectSlot}-keyed map of lists: each added element is credited to the active player. */
        SLOT_LISTS
    }

    private final Kind kind;

    EndOfTurnStateField(Kind kind) {
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }
}
