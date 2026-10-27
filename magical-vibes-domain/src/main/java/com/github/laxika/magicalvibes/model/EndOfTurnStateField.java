package com.github.laxika.magicalvibes.model;

/**
 * One "until end of turn" / "this turn" store that the cleanup step clears (CR 514.2), tracked so
 * that under Runed Terror's sequential phases the part of it created while one player was active
 * can outlive another player's cleanup step (see {@link EndOfTurnStateShare}). Most fields live on a
 * {@link Permanent} ({@link Permanent#captureEndOfTurnState()}); the {@code PLAYER_} fields live in
 * {@link GameData}'s per-player prevention shield maps; the {@code GAME_} fields are game-wide
 * {@link GameData} stores ({@link GameData#captureGameEndOfTurnState()}). A {@code GAME_} field backed by a
 * map of sets is a {@link Kind#SET} of its {@code Map.entry(key, element)} pairs, one backed by a map of
 * lists is a {@link Kind#LIST} of them, one backed by a plain map is a {@link Kind#SET} of its entries, and one
 * backed by a map of {@code int} counts is a {@link Kind#COUNTS} (a nested colour-count map is keyed by
 * {@code Map.entry(player, colour)}). A store that keeps only the strongest of several values (the highest damage
 * life floor, the tightest spell limit) is a {@link Kind#LIST} of every {@code (player, value)} contribution, re-merged
 * on restore, so a weaker value outlives the cleanup step that ends the stronger one.
 * Two aligned per-permanent lists (Soldevi Sentry's shield recipients and their controllers) are one
 * {@link Kind#LIST} of {@code (recipient, controller)} entries, so a share keeps them paired.
 * A one-shot or turn-long redirection / prevention shield list whose records are only ever added and removed
 * whole (Eye for an Eye, Comeuppance, Mirror Strike, Saving Grace, ...) is a {@link Kind#LIST} of its records, so
 * a shield used up in between is not restored. A shield list whose record is replaced by a reduced copy when it is
 * partly used (Healing Salve-style amount shields, the amount-limited redirections, Zealous Inquisitor) is a
 * {@link Kind#LIST} of its records too: the consuming code reports the replacement through
 * {@link GameData#replaceGameEndOfTurnElement}, so the reduced copy stays credited to the player active when the
 * shield was created. A list whose first matching shield applies first is restored in its live order. A record
 * rewritten in place to follow a spell that became a permanent changes only while that spell resolves, so in the
 * same player's phase that created it.
 * The pending "next spell you cast this turn" grants (Quicken, Savage Summoning, ...) are a {@link Kind#LIST} of
 * {@code (player, grant)} entries or a {@link Kind#COUNTS} by player; casting the spell consumes them, so a used
 * grant is not restored. Per-card "this turn" casting permissions and granted alternative costs (flashback,
 * unearth, graveyard / library-top cast permissions) are {@link Kind#SET}s of card ids or map entries.
 * The other pending one-shot "next spell / ability this turn" grants that are not paired with a source map (Storm,
 * copy, uncounterable and any-mana-type grants, the X-ability copies) are {@link Kind#COUNTS} by player or a
 * {@link Kind#LIST} of {@code (player, grant)} entries; the "this turn" delayed triggers (The Mirari Conjecture's copy
 * grants, Beck, Leori) and the source-linked exile / foretold cast grants are counts or {@link Kind#LIST}s of their
 * records. A grant used up in between is not restored.
 * The whole-turn chosen-source prevention (Auriok Replica, Samite Ministration) is a {@link Kind#SET} of
 * {@code (player, source)} entries, with its colour rider a {@link Kind#SET} of {@code ((player, source), colours)}
 * entries and its life-gain rider another {@code (player, source)} {@link Kind#SET}; Circle of Solace's one-shot
 * "next damage from a matching source" shields are a {@link Kind#LIST} of {@code (player, predicate)} entries, and
 * Storyweave's pending extra counters for the next enchantment creature entry are {@link Kind#COUNTS} by player
 * (a used shield or entry grant is not restored).
 * The "when you next cast an instant or sorcery spell / activate a loyalty or exhaust ability this turn, copy it"
 * delayed triggers keep their counts as {@link Kind#COUNTS} by player and their sources as a {@link Kind#LIST} of
 * {@code (player, source)} entries (the instant/sorcery count source, a single card per player, is a {@link Kind#SET}
 * of its map entries); a mana-value-limited copy is a {@link Kind#LIST} of {@code (player, max mana value)} entries,
 * a dynamic copy count and its source stay paired as one {@link Kind#LIST} of
 * {@code (player, GameData.PendingDynamicCopy)} entries, and Repeated Reverberation's linked players are a
 * {@link Kind#SET}. The next matching spell or ability consumes all of a player's pending copies at once, so a used
 * trigger is not restored. Sen Triplets' "play from that player's hand this turn" pair is a {@link Kind#VALUE}
 * {@code (controller, controlled player)} entry.
 * The per-card "you may play that card this turn" exile and graveyard permissions (impulse draw, Ark of Hunger) are
 * tracked through the {@link Kind#SET}s of card ids whose permission ends this turn; the cleanup step removes the
 * permission records only for the ids not retained for another player. Their current-turn any-mana-type riders are a
 * {@link Kind#SET} of card ids, and the source-linked "until end of turn, you may play cards exiled with it" grants
 * (Kayla's Music Box, Urianger Augurelt) a {@link Kind#SET} of {@code (source, cost modifier)} entries. A card
 * played or cast in between leaves exile or the graveyard, so its permission is not restored.
 * The "this turn" delayed-trigger watchers (Duskmantle / Vizkopa Guildmage, "whenever a creature dies this turn",
 * the "until end of turn" global triggered abilities) are {@link Kind#LIST}s of their records; the pending "the next
 * time you would draw a card this turn" replacements (the Words cycle, Aladdin's Lamp, Mangara's Tome, Ring of
 * Ma'rûf, Urabrask) are {@link Kind#COUNTS} by player or a {@link Kind#LIST} of {@code (player, replacement)}
 * entries, and Mystic Reflection's pending "next time creatures or planeswalkers enter this turn" copies a
 * {@link Kind#LIST} of its records. A replacement used up in between is not restored.
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
    OPPONENT_DRAW_REGENERATION_SHIELD(Kind.SHIELD),
    OPPONENT_DRAW_REGENERATION_SHIELD_GRANTS(Kind.LIST),
    PLUS_ONE_PLUS_ONE_COUNTER_REGENERATION_SHIELD(Kind.SHIELD),
    PLUS_ONE_PLUS_ONE_COUNTER_REGENERATION_SHIELD_CONTROLLERS(Kind.LIST),
    GAIN_CONTROL_REGENERATION_SHIELDS(Kind.LIST),
    PLAYER_DAMAGE_PREVENTION_SHIELD(Kind.SHIELD),
    PLAYER_COMBAT_DAMAGE_PREVENTION_SHIELD(Kind.SHIELD),
    GAME_DAMAGE_PREVENTION_SHIELD(Kind.SHIELD),
    GAME_PREVENT_ALL_COMBAT_DAMAGE(Kind.FLAG),
    GAME_PREVENT_ALL_COMBAT_DAMAGE_BY_ATTACKING_CREATURES(Kind.FLAG),
    GAME_PREVENT_ALL_COMBAT_DAMAGE_TO_PLAYERS(Kind.FLAG),
    GAME_PREVENT_ALL_DAMAGE_TO_ALL_CREATURES(Kind.FLAG),
    GAME_PREVENT_ALL_DAMAGE_BY_CREATURES(Kind.FLAG),
    GAME_DAMAGE_BY_CREATURES_PREVENTION_LIFE_GAIN_PLAYERS(Kind.LIST),
    GAME_PREVENT_ALL_DAMAGE_FROM_NON_HUMAN_SOURCES(Kind.FLAG),
    GAME_COMBAT_DAMAGE_EXEMPT_PREDICATES_BY_CONTROLLER(Kind.SET),
    GAME_PREVENT_DAMAGE_FROM_COLORS(Kind.SET),
    GAME_COLOR_DAMAGE_PREVENTION(Kind.SET),
    GAME_COLOR_DAMAGE_PREVENTION_FOR_CONTROLLED_CREATURES(Kind.SET),
    GAME_COLORLESS_DAMAGE_PREVENTION(Kind.SET),
    GAME_PERMANENTS_PREVENTED_FROM_DEALING_DAMAGE(Kind.SET),
    GAME_CREATURES_PROTECTED_FROM_TARGETING_DAMAGE(Kind.SET),
    GAME_PLAYERS_WITH_ALL_DAMAGE_PREVENTED(Kind.SET),
    GAME_PLAYERS_WITH_ALL_PERMANENT_DAMAGE_PREVENTED(Kind.SET),
    GAME_PLAYERS_WITH_ALL_CREATURE_DAMAGE_PREVENTED(Kind.SET),
    GAME_PLAYERS_WITH_ALL_PLAYER_DAMAGE_PREVENTED(Kind.SET),
    GAME_PLAYERS_WITH_ALL_COMBAT_DAMAGE_TO_PLAYER_AND_PLANESWALKERS_PREVENTED(Kind.SET),
    GAME_PLAYERS_WITH_DAMAGE_FROM_ATTACKERS_PREVENTED(Kind.SET),
    GAME_PLAYERS_WITH_DAMAGE_FROM_OPPONENT_CREATURES_PREVENTED(Kind.SET),
    GAME_PLAYERS_WITH_COMBAT_DAMAGE_FROM_TARGET_OPPONENT_CREATURES_PREVENTED(Kind.SET),
    GAME_PLAYERS_WITH_DAMAGE_FROM_MATCHING_SOURCES_PREVENTED(Kind.SET),
    GAME_PLAYERS_WITH_DAMAGE_TO_CONTROLLED_CREATURES_FROM_MATCHING_SOURCES_PREVENTED(Kind.SET),
    GAME_CREATURES_WITH_ALL_DAMAGE_PREVENTED(Kind.SET),
    GAME_PLAYERS_REDIRECTING_ALL_CREATURE_DAMAGE(Kind.SET),
    GAME_ALL_DAMAGE_PREVENTION_PREDICATES(Kind.SET),
    GAME_ALL_DAMAGE_PREVENTION_PREDICATES_BY_CONTROLLER(Kind.SET),
    GAME_COMBAT_DAMAGE_PREVENTION_PREDICATES_BY_CONTROLLER(Kind.SET),
    GAME_CREATURES_WITH_COMBAT_DAMAGE_PREVENTED(Kind.SET),
    GAME_CREATURES_PREVENTED_FROM_DEALING_COMBAT_DAMAGE(Kind.SET),
    GAME_CREATURES_ASSIGNING_NO_COMBAT_DAMAGE(Kind.SET),
    GAME_DAMAGE_CANT_BE_PREVENTED(Kind.FLAG),
    GAME_PLAYERS_CANT_GAIN_LIFE(Kind.FLAG),
    GAME_PLAYERS_CANT_LOSE_LIFE(Kind.FLAG),
    GAME_PLAYERS_CANT_SEARCH_LIBRARIES(Kind.FLAG),
    GAME_CREATURES_CANT_ATTACK(Kind.FLAG),
    GAME_PLAYERS_WHO_CANT_GAIN_LIFE(Kind.SET),
    GAME_PLAYERS_WHO_GAIN_NO_LIFE(Kind.SET),
    GAME_PLAYERS_WHO_CANT_LOSE_LIFE(Kind.SET),
    GAME_PLAYERS_WITH_HEXPROOF(Kind.SET),
    GAME_PLAYERS_WITH_SHROUD(Kind.SET),
    GAME_PLAYER_HEXPROOF_FROM_COLORS(Kind.SET),
    GAME_PERMANENT_HEXPROOF_FROM_COLORS(Kind.SET),
    GAME_PLAYER_PROTECTION_FROM_COLORS(Kind.SET),
    GAME_PLAYER_PROTECTION_FROM_PLAYERS(Kind.SET),
    GAME_PLAYER_KEYWORDS(Kind.SET),
    GAME_PLAYER_CREATURES_CANT_BE_TARGETED_BY_COLORS(Kind.SET),
    GAME_PLAYER_SPELLS_CANT_BE_COUNTERED_BY_COLORS(Kind.SET),
    GAME_PLAYERS_SPELLS_CANT_BE_COUNTERED(Kind.SET),
    GAME_PLAYERS_CREATURE_SPELLS_CANT_BE_COUNTERED(Kind.SET),
    GAME_PLAYERS_SILENCED(Kind.SET),
    GAME_PLAYERS_CANT_PLAY_LANDS(Kind.SET),
    GAME_PLAYERS_CANT_PLAY_FROM_GRAVEYARDS(Kind.SET),
    GAME_PLAYERS_CANT_CAST_SPELL_TYPES(Kind.SET),
    GAME_PLAYERS_CANT_CAST_NONCREATURE_SPELLS(Kind.SET),
    GAME_PLAYERS_CANT_ACTIVATE_ABILITIES(Kind.SET),
    GAME_PLAYERS_CANT_ACTIVATE_NON_MANA_ABILITIES(Kind.SET),
    GAME_PLAYERS_CANT_ACTIVATE_LOYALTY_ABILITIES(Kind.SET),
    GAME_PLAYER_CREATURES_CANT_ATTACK(Kind.LIST),
    GAME_PLAYERS_GATHERING_SPECIMENS(Kind.LIST),
    GAME_PLAYERS_GATHERING_TOKENS(Kind.LIST),
    GAME_PLAYERS_EXILING_UNCAST_ENTERING_CREATURES(Kind.SET),
    GAME_PLAYERS_EXILING_UNCAST_ENTERING_NONTOKEN_CREATURES(Kind.SET),
    GAME_PLAYERS_SHUFFLING_CREATURES_ENTERING_FROM_EXILE(Kind.SET),
    GAME_PLAYERS_EXILING_CREATURES_INSTEAD_OF_DYING(Kind.SET),
    GAME_PLAYERS_EXILING_OPPONENT_CREATURES_INSTEAD_OF_DYING(Kind.SET),
    GAME_PLAYERS_EXILING_CONTROLLED_PERMANENTS_INSTEAD_OF_DYING(Kind.SET),
    GAME_PLAYERS_EXILING_CARDS_INSTEAD_OF_GRAVEYARD(Kind.SET),
    GAME_PLAYERS_PUTTING_CARDS_ON_BOTTOM_OF_LIBRARY(Kind.SET),
    GAME_PLAYERS_WITH_FLASH(Kind.SET),
    GAME_PLAYERS_WITH_FREE_HAND_CAST(Kind.SET),
    GAME_PLAYERS_WHO_MAY_LOOK_AT_FACE_DOWN_CREATURES(Kind.SET),
    GAME_PLAYERS_MAY_PLAY_FACE_UP_CARDS_FROM_EXILE(Kind.SET),
    GAME_CARD_TYPE_FLASH_GRANTS(Kind.SET),
    GAME_PLAYERS_ALLOWED_TWICE_LOYALTY_ACTIVATIONS(Kind.SET),
    GAME_GRAVEYARD_CARD_ANIMATIONS(Kind.SET),
    GAME_PLAYER_COLOR_DAMAGE_PREVENTION_COUNTS(Kind.COUNTS),
    GAME_COMBAT_DAMAGE_TO_CREATURES_DOUBLINGS(Kind.ADDITIVE),
    GAME_CONTROLLER_DAMAGE_DOUBLINGS(Kind.COUNTS),
    GAME_PERMANENT_DAMAGE_DOUBLINGS(Kind.COUNTS),
    GAME_COLOR_SOURCE_DAMAGE_BONUSES(Kind.LIST),
    GAME_CONTROLLER_DAMAGE_BONUSES(Kind.LIST),
    GAME_DAMAGE_REPLACEMENTS(Kind.LIST),
    GAME_ALL_PERMANENTS_ENTER_TAPPED(Kind.FLAG),
    GAME_PERMANENT_ENTER_TAPPED_FILTERS(Kind.SET),
    GAME_ADDITIONAL_ENTER_COUNTERS(Kind.COUNTS),
    GAME_MATCHING_CREATURE_BLOCK_RESTRICTIONS(Kind.LIST),
    GAME_COUNTER_HOLDER_MUST_BLOCK_REQUIREMENTS(Kind.SET),
    GAME_PLAYER_EXTRA_LOYALTY_ACTIVATIONS(Kind.COUNTS),
    GAME_EXTRA_MANA_ON_LAND_SUBTYPE_TAP(Kind.LIST),
    GAME_LAND_SUBTYPE_FIXED_MANA_COLOR(Kind.SET),
    GAME_NONBASIC_LANDS_FIXED_MANA_COLOR(Kind.VALUE),
    GAME_ALL_LANDS_FIXED_MANA_COLOR(Kind.VALUE),
    GAME_PLAYERS_WITH_COLORED_MANA_REPLACEMENT(Kind.SET),
    GAME_PLAYERS_WITH_WHITE_MANA_AS_ANY_COLOR(Kind.SET),
    GAME_PLAYERS_WITH_LAND_MANA_CHOICE_REPLACEMENT(Kind.SET),
    GAME_LAND_MANA_FIXED_COLOR(Kind.SET),
    GAME_PLAYER_STATIC_EFFECTS(Kind.LIST),
    GAME_DAMAGE_LIFE_FLOORS(Kind.LIST),
    GAME_SPELL_LIMITS(Kind.LIST),
    GAME_CHANNEL_HARM_SHIELDS(Kind.LIST),
    GAME_COMEUPPANCE_SHIELDS(Kind.LIST),
    GAME_JUDGMENT_OF_ALEXANDER_SHIELDS(Kind.LIST),
    GAME_EYE_FOR_AN_EYE_SHIELDS(Kind.LIST),
    GAME_REFLECT_DAMAGE_TO_SOURCE_CONTROLLER_SHIELDS(Kind.LIST),
    GAME_SOURCE_NEXT_COMBAT_DAMAGE_TO_CONTROLLER_SHIELDS(Kind.LIST),
    GAME_SOURCE_NEXT_COMBAT_DAMAGE_TO_OPPONENT_REDIRECT_SHIELDS(Kind.LIST),
    GAME_SOURCE_NEXT_DAMAGE_REDIRECT_TO_PERMANENT_SHIELDS(Kind.LIST),
    GAME_PLAYER_NEXT_INSTANT_OR_SORCERY_DAMAGE_REDIRECT_SHIELDS(Kind.LIST),
    GAME_PLAYER_SOURCE_NEXT_DAMAGE_REDIRECT_SHIELDS(Kind.LIST),
    GAME_TURN_SOURCE_DAMAGE_REDIRECT_TO_CONTROLLER_SHIELDS(Kind.LIST),
    GAME_TURN_DAMAGE_REDIRECT_TO_CREATURE_SHIELDS(Kind.LIST),
    GAME_TARGET_SORCERY_DAMAGE_REDIRECT_SHIELDS(Kind.LIST),
    GAME_COMBAT_DAMAGE_PREVENTION_TOKEN_SHIELDS(Kind.SET),
    GAME_NEXT_SPELL_FLASH_GRANTS(Kind.LIST),
    GAME_NEXT_SPELL_CHOSEN_SUBTYPE_FLASH_GRANTS(Kind.LIST),
    GAME_NEXT_SPELL_CONVOKE_GRANTS(Kind.COUNTS),
    GAME_NEXT_SPELL_COST_REDUCTIONS(Kind.LIST),
    GAME_NEXT_SPELL_PAY_LIFE_EQUAL_TO_MANA_VALUE(Kind.COUNTS),
    GAME_NEXT_SPELL_FREE_CAST_PERMISSIONS(Kind.LIST),
    GAME_NEXT_CREATURE_SPELL_EMPOWERMENTS(Kind.LIST),
    GAME_NEXT_CREATURE_SPELL_DELAYED_TRIGGERS(Kind.LIST),
    GAME_NEXT_CREATURE_SPELL_CASCADE(Kind.COUNTS),
    GAME_SPELL_COLOR_OVERRIDES(Kind.SET),
    GAME_CARDS_GRANTED_FLASHBACK(Kind.SET),
    GAME_CARDS_GRANTED_FLASHBACK_DESPITE_LOST_ABILITIES(Kind.SET),
    GAME_CARDS_GRANTED_FLASHBACK_COSTS(Kind.SET),
    GAME_CARDS_GRANTED_WARP(Kind.SET),
    GAME_CARDS_GRANTED_HARMONIZE(Kind.SET),
    GAME_CARDS_GRANTED_JUMP_START(Kind.SET),
    GAME_CARDS_GRANTED_EMBALM(Kind.SET),
    GAME_CARDS_GRANTED_UNEARTH(Kind.SET),
    GAME_CARDS_GRANTED_UNEARTH_GRAVEYARD_VERSIONS(Kind.SET),
    GAME_LIBRARY_TOP_CARD_FREE_PLAY_PERMISSIONS(Kind.SET),
    GAME_LIBRARY_TOP_CARD_PERMISSIONS(Kind.SET),
    GAME_PLAYERS_ALLOWED_TO_PLAY_FROM_LIBRARY_TOP(Kind.SET),
    GAME_LIBRARY_TOP_CARD_LIFE_PLAY_PERMISSIONS(Kind.SET),
    GAME_MAY_TAP_LANDS_FOR_SPELLS(Kind.SET),
    GAME_MAY_PAY_LIFE_FOR_COLORLESS_MANA(Kind.SET),
    GAME_GUARDIAN_ANGEL_TARGETS(Kind.SET),
    GAME_MAY_CAST_TOP_INSTANT_OR_SORCERY_FROM_GRAVEYARD(Kind.SET),
    GAME_GRAVEYARD_CARD_CAST_PERMISSIONS(Kind.SET),
    GAME_GRAVEYARD_CARD_CAST_PERMISSION_GRANTS(Kind.LIST),
    GAME_GRAVEYARD_CAST_FILTER_PERMISSIONS(Kind.LIST),
    GAME_GRAVEYARD_PLAY_FILTER_PERMISSIONS(Kind.LIST),
    GAME_SPELL_COPY_GRANTS(Kind.COUNTS),
    GAME_NEXT_INSTANT_SORCERY_STORM(Kind.COUNTS),
    GAME_NEXT_INSTANT_SORCERY_CAST_FROM_HAND_TO_HAND(Kind.COUNTS),
    GAME_NEXT_SPELL_COPIES(Kind.COUNTS),
    GAME_NEXT_FILTERED_SPELL_COPIES(Kind.LIST),
    GAME_NEXT_X_ACTIVATED_ABILITY_COPIES(Kind.COUNTS),
    GAME_NEXT_SPELL_UNCOUNTERABLE(Kind.COUNTS),
    GAME_NEXT_INSTANT_SORCERY_UNCOUNTERABLE(Kind.COUNTS),
    GAME_ANY_MANA_TYPE_FOR_NEXT_SPELL(Kind.COUNTS),
    GAME_CHOSEN_SUBTYPE_ACTIVATION_COPIES(Kind.LIST),
    GAME_CREATURE_ENTERS_DRAW_SOURCES(Kind.LIST),
    GAME_UNTAP_ATTACKED_CREATURES_EACH_COMBAT_SOURCES(Kind.LIST),
    GAME_EXILE_CAST_PERMISSIONS(Kind.LIST),
    GAME_EXILE_CAST_WILDCARD_PERMISSIONS(Kind.LIST),
    GAME_FORETOLD_CARD_CAST_PERMISSIONS(Kind.LIST),
    GAME_PLAYER_SOURCE_DAMAGE_PREVENTION_IDS(Kind.SET),
    GAME_PLAYER_SOURCE_DAMAGE_PREVENTION_REQUIRED_COLORS(Kind.SET),
    GAME_PLAYER_SOURCE_DAMAGE_PREVENTION_LIFE_GAIN_IDS(Kind.SET),
    GAME_PLAYER_NEXT_DAMAGE_FROM_MATCHING_SOURCES_PREVENTED(Kind.LIST),
    GAME_NEXT_ENCHANTMENT_CREATURE_ENTRY_ADDITIONAL_COUNTERS(Kind.COUNTS),
    GAME_NEXT_INSTANT_SORCERY_COPIES(Kind.COUNTS),
    GAME_NEXT_INSTANT_SORCERY_COPY_SINGLE_TRIGGERS(Kind.COUNTS),
    GAME_NEXT_INSTANT_SORCERY_COPY_SOURCES(Kind.SET),
    GAME_NEXT_INSTANT_SORCERY_COPY_MAX_MANA_VALUES(Kind.LIST),
    GAME_NEXT_INSTANT_SORCERY_DYNAMIC_COPIES(Kind.LIST),
    GAME_NEXT_LOYALTY_ABILITY_COPIES(Kind.COUNTS),
    GAME_NEXT_LOYALTY_ABILITY_COPY_SOURCES(Kind.LIST),
    GAME_NEXT_SPELL_OR_LOYALTY_COPY_LINKED(Kind.SET),
    GAME_NEXT_EXHAUST_ABILITY_COPIES(Kind.COUNTS),
    GAME_NEXT_EXHAUST_ABILITY_COPY_SOURCES(Kind.LIST),
    GAME_SEN_TRIPLETS_HAND_PERMISSION(Kind.VALUE),
    GAME_EXILE_PLAY_PERMISSIONS_ENDING_THIS_TURN(Kind.SET),
    GAME_GRAVEYARD_PLAY_PERMISSIONS_ENDING_THIS_TURN(Kind.SET),
    GAME_EXILE_PLAY_GRANTS_BY_SOURCE(Kind.SET),
    GAME_EXILE_PLAY_ANY_MANA_TYPE_THIS_TURN(Kind.SET),
    GAME_PLAYER_SOURCE_NEXT_DAMAGE_SHIELDS(Kind.LIST),
    GAME_SOURCE_NEXT_DAMAGE_TO_ANY_TARGET_SHIELDS(Kind.LIST),
    GAME_TARGET_SPELL_DAMAGE_PREVENTION_SHIELDS(Kind.LIST),
    GAME_SOURCE_PERMANENT_AND_CONTROLLER_NEXT_DAMAGE_REDIRECT_SHIELDS(Kind.LIST),
    GAME_DAMAGE_REDIRECT_SHIELDS(Kind.LIST),
    GAME_SOURCE_DAMAGE_REDIRECT_SHIELDS(Kind.LIST),
    GAME_TARGET_SOURCE_DAMAGE_PREVENTION_SHIELDS(Kind.LIST),
    GAME_DAMAGE_PREVENTION_LIFE_GAIN_SHIELDS(Kind.LIST),
    GAME_PLAYER_NEXT_DAMAGE_REDIRECT_SHIELDS(Kind.LIST),
    GAME_CREATURE_DAMAGE_REDIRECT_SHIELDS(Kind.LIST),
    GAME_EXILE_INSTEAD_OF_GRAVEYARD(Kind.SET),
    GAME_OUTSIDE_GAME_PLAY_PERMISSIONS(Kind.LIST),
    GAME_OUTSIDE_GAME_ADDITIONAL_MODAL_MODE_PERMISSIONS(Kind.SET),
    GAME_OPPONENT_GRAVEYARD_LIFE_LOSS_WATCHERS(Kind.LIST),
    GAME_LIFE_GAIN_OPPONENT_LIFE_LOSS_WATCHERS(Kind.LIST),
    GAME_CREATURE_DEATH_TRIGGER_WATCHERS(Kind.LIST),
    GAME_DAMAGED_CREATURE_DEATH_TRIGGER_WATCHERS(Kind.LIST),
    GAME_ALLY_CREATURE_ENTERS_TRIGGER_WATCHERS(Kind.LIST),
    GAME_TEMPORARY_GLOBAL_TRIGGERED_ABILITIES_THIS_TURN(Kind.LIST),
    GAME_NEXT_DRAW_LOOK_AT_TOP(Kind.LIST),
    GAME_NEXT_DRAW_GAIN_LIFE(Kind.COUNTS),
    GAME_NEXT_DRAW_CREATE_BEARS(Kind.LIST),
    GAME_NEXT_DRAW_DAMAGE(Kind.LIST),
    GAME_NEXT_DRAW_RETURN_PERMANENTS(Kind.COUNTS),
    GAME_NEXT_DRAW_DISCARD_OPPONENTS(Kind.COUNTS),
    GAME_NEXT_DRAW_FROM_EXILED_PILE(Kind.LIST),
    GAME_NEXT_DRAW_FROM_OUTSIDE_GAME(Kind.COUNTS),
    GAME_NEXT_DRAW_EXILE_TOP_CARD(Kind.COUNTS),
    GAME_PENDING_MYSTIC_REFLECTIONS(Kind.LIST);

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
