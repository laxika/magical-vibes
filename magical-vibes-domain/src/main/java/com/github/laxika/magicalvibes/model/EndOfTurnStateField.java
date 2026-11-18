package com.github.laxika.magicalvibes.model;

/**
 * One "until end of turn" / "this turn" store that the cleanup step clears (CR 514.2), tracked so
 * that under Runed Terror's sequential phases the part of it created while one player was active
 * can outlive another player's cleanup step (see {@link EndOfTurnStateShare}). Most fields live on a
 * {@link Permanent} ({@link Permanent#captureEndOfTurnState()}); the {@code GAME_} fields are game-wide
 * {@link GameData} stores ({@link GameData#captureGameEndOfTurnState()}).
 *
 * <p>An effect a resolution makes is not kept here: it is a floating effect with a {@code Duration}, and the
 * duration clock ends it (Runed Terror's per-player cleanup included). What is left of the game-wide stores:
 * the "this turn" delayed-trigger watchers and "until end of turn" global triggered abilities
 * ({@link Kind#LIST}s of their records, so one used in between is not restored); Stolen Uniform's "when you lose
 * control of that Equipment this turn" triggers, a {@link Kind#LIST} of {@code (equipment, trigger)} entries that
 * the cleanup step drops only after control is recomputed, so they can still fire as the control effect ends; and
 * the turn-history records. Those follow the Runed Terror ruling that a player's "each of your turns" permission
 * resets after that player's ending phase: the speed, play-from-exile, cast-from-outside-hand, foretell and
 * first-non-draw-step-draw records, the game's exiled-card count, and the left-the-graveyard and creature-exile
 * records ({@link Kind#COUNTS} by player for the counts), with a permanent's regeneration count, the Merfolk tapped
 * for Vodalian War Machine and the creatures that crewed a Vehicle. Each record made while a player was active lasts
 * until that player's cleanup step. A {@code GAME_} field backed by a map of lists is a {@link Kind#LIST} of its
 * {@code Map.entry(key, element)} pairs, and one backed by a map of {@code int} counts is a {@link Kind#COUNTS}.
 *
 * <p>Bookkeeping of one draw or one spell that cannot be pending across a cleanup step
 * ({@code GameData.chainsDrawReplacementsApplied}, {@code drawStepFirstDrawTaken},
 * {@code spellsPaidUsingPendingAnyManaTypeThisTurn}) has no field here.
 *
 * <p>A new until-end-of-turn field of {@link Permanent} needs a constant here and its cases in
 * {@code Permanent.endOfTurnStateValue} and {@code addEndOfTurnStateValue}, beside its line in
 * {@link Permanent#resetModifiers()}. A new game-wide turn-history store cleared by
 * {@code TurnCleanupService.resetEndOfTurnModifiers} needs a {@code GAME_} constant and its cases in
 * {@code GameData.gameEndOfTurnStateValue} and {@code addGameEndOfTurnStateValue}.
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
    TEMPORARY_ABILITIES(Kind.LIST),
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
    DAMAGE_REPLACED_BY_PLUS_ONE_PLUS_ONE_COUNTERS(Kind.FLAG),
    DAMAGE_DESTRUCTION_SHIELD(Kind.SHIELD),
    LAND_DESTRUCTION_SHIELD(Kind.SHIELD),
    REGENERATION_SHIELD(Kind.SHIELD),
    MINUS_ONE_COUNTER_REGENERATION_SHIELD(Kind.SHIELD),
    OPPONENT_DRAW_REGENERATION_SHIELD(Kind.SHIELD),
    OPPONENT_DRAW_REGENERATION_SHIELD_GRANTS(Kind.LIST),
    PLUS_ONE_PLUS_ONE_COUNTER_REGENERATION_SHIELD(Kind.SHIELD),
    PLUS_ONE_PLUS_ONE_COUNTER_REGENERATION_SHIELD_CONTROLLERS(Kind.LIST),
    GAIN_CONTROL_REGENERATION_SHIELDS(Kind.LIST),
    TIMES_REGENERATED(Kind.ADDITIVE),
    TAPPED_PERMANENTS_FOR_ABILITY(Kind.LIST),
    CREATURES_THAT_CREWED(Kind.SET),
    GAME_OPPONENT_GRAVEYARD_LIFE_LOSS_WATCHERS(Kind.LIST),
    GAME_LIFE_GAIN_OPPONENT_LIFE_LOSS_WATCHERS(Kind.LIST),
    GAME_ALLY_CREATURE_ENTERS_TRIGGER_WATCHERS(Kind.LIST),
    GAME_TEMPORARY_GLOBAL_TRIGGERED_ABILITIES_THIS_TURN(Kind.LIST),
    GAME_CONTROL_LOSS_UNATTACH_TRIGGERS(Kind.LIST),
    GAME_PLAYERS_WHOSE_SPEED_INCREASED(Kind.SET),
    GAME_PLAYERS_WHO_PLAYED_CARD_FROM_EXILE(Kind.SET),
    GAME_PLAYERS_WHO_PLAYED_OR_CAST_FROM_OUTSIDE_HAND(Kind.SET),
    GAME_PLAYERS_WHO_FORETOLD(Kind.SET),
    GAME_CARDS_EXILED(Kind.ADDITIVE),
    GAME_FIRST_NON_DRAW_STEP_DRAW_REPLACEMENTS_USED(Kind.SET),
    GAME_PLAYERS_WHOSE_CARDS_LEFT_GRAVEYARD(Kind.SET),
    GAME_PLAYERS_WHOSE_CREATURE_CARDS_LEFT_GRAVEYARD(Kind.SET),
    GAME_CARDS_LEFT_GRAVEYARD_COUNTS(Kind.COUNTS),
    GAME_CREATURE_EXILE_COUNTS(Kind.COUNTS);

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
        /** A map of {@code int} counts by key (per-key doublings, extra activations, one-shot colour
         *  shields): each player keeps their per-key delta, never more than what is left for that key. */
        COUNTS,
        /** A list compared by identity (CR 611.2c effect instances). */
        IDENTITY_LIST
    }

    private final Kind kind;

    EndOfTurnStateField(Kind kind) {
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }
}
