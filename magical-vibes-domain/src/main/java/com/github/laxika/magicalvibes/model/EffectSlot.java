package com.github.laxika.magicalvibes.model;

public enum EffectSlot {
    ON_TAP,
    ON_ENTER_BATTLEFIELD,
    SPELL,
ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
    ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
    ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD,
    ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
    STATIC,
    ON_SACRIFICE,
    ON_BLOCK,
    UPKEEP_TRIGGERED,
    GRAVEYARD_UPKEEP_TRIGGERED,
    EACH_UPKEEP_TRIGGERED,
    OPPONENT_UPKEEP_TRIGGERED,
    ON_ANY_PLAYER_CASTS_SPELL,
    ON_CONTROLLER_CASTS_SPELL,
    ON_OPPONENT_CASTS_SPELL,
    ON_DEATH,
    ON_ALLY_CREATURE_DIES,
    ON_DAMAGED_CREATURE_DIES,
    ON_COMBAT_DAMAGE_TO_PLAYER,
    ON_COMBAT_DAMAGE_TO_CREATURE,
    ON_DAMAGE_TO_PLAYER,
    ON_ATTACK,
    ON_BECOMES_BLOCKED,
    DRAW_TRIGGERED,
    EACH_DRAW_TRIGGERED,
    END_STEP_TRIGGERED,
    CONTROLLER_END_STEP_TRIGGERED,
    ON_CONTROLLER_DRAWS,
    ON_OPPONENT_DRAWS,
    ON_OPPONENT_DISCARDS,
    ON_SELF_DISCARDED_BY_OPPONENT,
    ON_ANY_PLAYER_TAPS_LAND,
    ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU,
    ON_ALLY_PERMANENT_SACRIFICED,
    ON_BECOMES_TARGET_OF_SPELL,
    ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
    ON_ANY_CREATURE_DIES,
    ON_ALLY_NONTOKEN_CREATURE_DIES,
    ON_ANY_NONTOKEN_CREATURE_DIES,
    ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
    ON_ENCHANTED_PERMANENT_TAPPED,
    ON_EQUIPPED_CREATURE_DIES,
    ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
    ON_OPPONENT_LAND_ENTERS_BATTLEFIELD,
    /** Triggers whenever a land the controller controls enters the battlefield.
     *  Checked in {@code BattlefieldEntryService.checkAllyLandEntersTriggers}. */
    ON_ALLY_LAND_ENTERS_BATTLEFIELD,
    ON_OPPONENT_CREATURE_DIES,
    ON_DEALT_DAMAGE,
    ON_OPENING_HAND_REVEAL,
    ON_OPPONENT_LOSES_LIFE,
    ON_OPPONENT_SHUFFLES_LIBRARY,
    ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
    ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
    ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD,
    ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD,
    ON_CONTROLLER_GAINS_LIFE,
    ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE,
    ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
    ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
    ON_OPPONENT_CREATURE_CARD_MILLED,
    ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD,
    /** Triggers when this card is put into its owner's graveyard from their library (milled).
     *  Checked per-card inside {@code GraveyardService.resolveMillPlayer}. */
    ON_SELF_MILLED,
    /** Triggers once when one or more creatures the controller controls are declared as attackers.
     *  Unlike ON_ATTACK (which fires per creature), this fires exactly once per combat. */
    ON_ALLY_CREATURES_ATTACK,
    /** State-triggered abilities (MTG rule 603.8). Checked after SBAs; fire once onto the
     *  stack and don't retrigger while the ability is already on the stack. */
    STATE_TRIGGERED,
    /** Saga chapter I ability (MTG rule 714). Triggers when the first lore counter is placed. */
    SAGA_CHAPTER_I,
    /** Saga chapter II ability (MTG rule 714). Triggers when the second lore counter is placed. */
    SAGA_CHAPTER_II,
    /** Saga chapter III ability (MTG rule 714). Triggers when the third lore counter is placed. */
    SAGA_CHAPTER_III,
    /** Triggers at the beginning of combat on the controller's turn.
     *  Checked in {@code StepTriggerService.handleBeginningOfCombatTriggers}. */
    BEGINNING_OF_COMBAT_TRIGGERED,
    /** Triggers whenever a creature an opponent controls is dealt damage (combat or non-combat).
     *  Fires on the permanent with this slot, not on the damaged creature. Scans all battlefields
     *  for permanents with this slot whose controller is different from the damaged creature's controller. */
    ON_OPPONENT_CREATURE_DEALT_DAMAGE,
    /** Triggers when the controller casts a spell matching the filter, while this card is in
     *  the controller's graveyard.  Checked per-card inside
     *  {@code TriggerCollectionService.checkSpellCastTriggers}. */
    GRAVEYARD_ON_CONTROLLER_CASTS_SPELL,
    /** Triggers whenever the controller of this permanent loses life (damage or direct life loss).
     *  Fires on the controller's own permanents. The amount is passed via TriggerContext.LifeLoss.
     *  Hooked into TriggerCollectionService.checkLifeLossTriggers(). Used by Lich's Mastery. */
    ON_CONTROLLER_LOSES_LIFE,
    /** Triggers when this permanent leaves the battlefield by any means (destruction, exile,
     *  bounce, sacrifice, tuck). Checked in PermanentRemovalService after removal. */
    ON_SELF_LEAVES_BATTLEFIELD,
    /** Triggers whenever an Aura or Equipment controlled by the same player is put into a
     *  graveyard from the battlefield. Checked in DeathTriggerService after the card enters
     *  the graveyard. Used by Tiana, Ship's Caretaker. */
    ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD
}
