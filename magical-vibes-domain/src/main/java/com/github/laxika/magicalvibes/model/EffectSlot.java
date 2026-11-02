package com.github.laxika.magicalvibes.model;

public enum EffectSlot {
    PLANESWALK_TO_TRIGGERED,
    PLANESWALK_FROM_TRIGGERED,
    CHAOS_TRIGGERED,
    ENCOUNTER_TRIGGERED,
    /** Triggers when this permanent changes control to another player. */
    ON_SELF_BECOMES_CONTROLLED,
    /** Triggers when this permanent changes control away from its current controller. */
    ON_SELF_LOSES_CONTROL,
    /** Triggers when this permanent's controller sets a scheme in motion. */
    ON_CONTROLLER_SETS_SCHEME_IN_MOTION,
    ON_CONTROLLER_GIVES_GIFT,
    /** Triggers whenever a Case is solved by the controller. */
    ON_ALLY_CASE_SOLVES,
    ON_CONTROLLER_FORETELLS,
    ON_CONTROLLER_FORAGES,
    /** Triggers after all players finish a voting event. */
    ON_PLAYERS_FINISH_VOTING,
    /** Triggers whenever this permanent's controller chooses a Ring-bearer after the Ring tempts them. */
    ON_CONTROLLER_TEMPTS_RING,
    /** Triggers when this permanent's controller investigates for the first time each turn. */
    ON_CONTROLLER_INVESTIGATES,
    /** Triggers whenever this permanent's controller investigates one or more times. */
    ON_CONTROLLER_INVESTIGATES_EACH_TIME,
    /** Triggers whenever this permanent's controller manifests dread. */
    ON_CONTROLLER_MANIFESTS_DREAD,
    /** Triggers whenever this permanent's controller completes a dungeon. */
    ON_CONTROLLER_COMPLETES_DUNGEON,
    /** Triggers whenever this permanent's controller rolls one or more dice. */
    ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE,
    /** Triggers once for each die its controller rolls, carrying that die's result as the event value
     *  ("Whenever you roll a 4 or higher" is an intervening-if on {@code EventValueAtLeast}). */
    ON_CONTROLLER_ROLLS_DIE,
    /** Triggers once for each die showing its unmodified highest natural result. */
    ON_CONTROLLER_ROLLS_HIGHEST_NATURAL_RESULT,
    ON_CONTROLLER_BENDS,
    /** Triggers whenever this permanent's controller exerts a creature. */
    ON_CONTROLLER_EXERTS,
    /** Triggers whenever this permanent's controller collects evidence. */
    ON_CONTROLLER_COLLECTS_EVIDENCE,
    /** Triggers whenever this permanent's controller completes a discover action. */
    ON_CONTROLLER_DISCOVERS,
    /** Triggers whenever this permanent's controller seeks one or more cards. */
    ON_CONTROLLER_SEEKS,
    /** Triggers whenever this permanent's controller conjures one or more cards. */
    ON_CONTROLLER_CONJURES,
    /** Triggers whenever an opponent gains control of a permanent from this permanent's controller. */
    ON_OPPONENT_GAINS_CONTROL_OF_YOUR_PERMANENT,
    /** Triggers on a permanent whenever an Aura becomes attached to it — both when an Aura spell
     *  resolves onto it and when an already-resolved Aura is moved onto it. Queued as a
     *  non-targeting triggered ability whose {@code sourcePermanentId} is the enchanted permanent,
     *  controlled by that permanent's controller (so it fires for an opponent's Aura too).
     *  Checked in {@code TriggerCollectionService.checkAuraAttachedTriggers}. Used by Brood Keeper. */
    ON_AURA_ATTACHED_TO_SELF,
    /** Triggers whenever an Aura controlled by this permanent's controller becomes attached to a creature controlled by that player. */
    ON_ALLY_AURA_ATTACHED_TO_ALLY_CREATURE,
    /** Triggers on an Equipment whenever it becomes attached to a permanent. */
    ON_EQUIPMENT_ATTACHED,
    /** Triggers whenever this creature is tapped to pay the crew cost of a Vehicle. The
     *  triggering Vehicle is stored on the resulting stack entry for effects that refer to it.
     *  Checked from {@code CrewCostHandler}. */
    ON_CREWS_VEHICLE,
    /** Triggers whenever a creature the controller controls is tapped to pay a Vehicle's crew cost.
     *  The event context carries both the crewing creature and the Vehicle. Checked from
     *  {@code CrewCostHandler}; the source permanent itself is included. */
    ON_ALLY_CREATURE_CREWS_VEHICLE,
    /** Triggers whenever the game changes between day and night. */
    ON_DAY_NIGHT_CHANGE,
    /** Triggers whenever the game changes between day and night while this card is in its controller's graveyard. */
    GRAVEYARD_ON_DAY_NIGHT_CHANGE,
    /** Triggers whenever the controller fully unlocks a Room. */
    ON_ALLY_ROOM_FULLY_UNLOCKED,
    /** Triggers whenever the controller fully unlocks a Room while this card is in the controller's
     *  graveyard. Checked in {@code TriggerCollectionService.checkAllyRoomFullyUnlockedTriggers}. */
    GRAVEYARD_ON_ALLY_ROOM_FULLY_UNLOCKED,
    /** Triggers whenever this Room's specified door becomes unlocked. */
    ON_SELF_ROOM_DOOR_UNLOCKED,
    /** Triggers whenever this permanent's controller becomes the monarch. */
    ON_CONTROLLER_BECOMES_MONARCH,
    /** Triggers whenever an opponent of this permanent's controller becomes the monarch. */
    ON_OPPONENT_BECOMES_MONARCH,
    /** Triggers whenever this permanent's controller is tempted by the Ring. */
    ON_RING_TEMPTS_YOU,
    /** Triggers whenever this permanent's controller wins a coin flip. */
    ON_CONTROLLER_WINS_COIN_FLIP,
    /** Triggers whenever any player wins a coin flip. */
    ON_ANY_PLAYER_WINS_COIN_FLIP,
    /** Triggers whenever this permanent's controller loses a coin flip. */
    ON_CONTROLLER_LOSES_COIN_FLIP,
    /** State-triggered abilities (MTG rule 603.8). Checked after SBAs; fire once onto the
     *  stack and don't retrigger while the ability is already on the stack. */
    STATE_TRIGGERED,
    /** Saga chapter I ability (MTG rule 714). Triggers when the first lore counter is placed. */
    SAGA_CHAPTER_I,
    /** Saga chapter II ability (MTG rule 714). Triggers when the second lore counter is placed. */
    SAGA_CHAPTER_II,
    /** Saga chapter III ability (MTG rule 714). Triggers when the third lore counter is placed. */
    SAGA_CHAPTER_III,
    /** Saga chapter IV ability. Triggers when the fourth lore counter is placed. */
    SAGA_CHAPTER_IV,
    /** Saga chapter V ability. Triggers when the fifth lore counter is placed. */
    SAGA_CHAPTER_V,
    /** Saga chapter VI ability. Triggers when the sixth lore counter is placed. */
    SAGA_CHAPTER_VI,
    /** Triggers when the final chapter ability of a Saga finishes resolving. */
    ON_SAGA_FINAL_CHAPTER_ABILITY_RESOLVES,
    /** Triggers when the final chapter ability of a Saga triggers, i.e. is put on the stack (Historian's Boon). */
    ON_SAGA_FINAL_CHAPTER_ABILITY_TRIGGERS,

    /** Triggers whenever a creature controlled by this permanent's controller fights. */
    ON_ALLY_CREATURE_FIGHTS,
    /** Triggers whenever the controller completes a dungeon, while this card is in their graveyard. */
    GRAVEYARD_ON_CONTROLLER_COMPLETES_DUNGEON,
    /** Triggers whenever the controller conjures one or more cards, while this card is in the
     *  controller's graveyard. Checked per-card inside {@code TriggerCollectionService.checkConjureTriggers}. */
    GRAVEYARD_ON_CONTROLLER_CONJURES,
    /** Triggers whenever the controller rolls a natural 20, while this card is in their graveyard. */
    GRAVEYARD_ON_CONTROLLER_ROLLS_NATURAL_20,
    /** Triggers whenever an opponent pays a mana tax imposed by a spell or permanent controlled by this permanent. */
    ON_OPPONENT_PAYS_TAX,
    /** Triggers whenever a creature controlled by the same player explores.
     *  Fired from {@code ExploreEffectHandler} (land branch) and
     *  {@code MayMiscHandlerService} (non-land branch) after explore completes. */
    ON_ALLY_CREATURE_EXPLORES,
    /** Triggers whenever a creature controlled by the same player connives. */
    ON_ALLY_CREATURE_CONNIVES,
    /** Triggers when this permanent exploits a creature (CR 702.110): its controller sacrificed
     *  a creature as its {@code ExploitEffect} ETB ability resolved, and this permanent was still
     *  on the battlefield at the start of that resolution (sacrificing itself still counts).
     *  Fired from the exploit sacrifice completion path. Used by Overcharged Amalgam. */
    ON_EXPLOIT,
    /** Triggers whenever a creature controlled by this permanent's controller exploits a creature. */
    ON_ALLY_CREATURE_EXPLOITS,
    /** Triggers whenever a creature controlled by this permanent's controller exploits a nontoken
     *  creature. Fired after the exploit sacrifice completes. */
    ON_ALLY_CREATURE_EXPLOITS_NONTOKEN_CREATURE,
    /** Triggers whenever the controller clashes (MTG rule 701.29). Fired from
     *  {@code TriggerCollectionService.performClash} after the clash ends. Targeting triggers route
     *  through the {@code PermanentChoiceContext.ClashTriggerTarget} interaction so the controller
     *  chooses a target creature an opponent controls (Entangling Trap); non-targeting triggers go
     *  straight onto the stack as a triggered ability (Rebellion of the Flamekin). Effects wrapped in
     *  {@code IfWonClashEffect} apply only when the controller won the clash, and effects wrapped in
     *  {@code IfLostClashEffect} only when they did not win — exactly one branch fires when both are
     *  listed. */
    ON_CONTROLLER_CLASHES,
    /** Triggers whenever a player loses the game. Fired from {@code GameOutcomeService}
     *  at the moment a player is determined to lose (life/poison loss in
     *  {@code checkWinCondition}, or a direct loss via {@code declareWinner}).
     *  Note: this engine is strictly 2-player and the game ends the instant a player
     *  loses, so in practice this trigger goes onto the stack but the game finishes
     *  before it can resolve. Used by Withengar Unbound. */
    ON_PLAYER_LOSES_GAME,
    /** Triggers when a creature is championed with this permanent (i.e. exiled by this permanent's
     *  Champion ability). Fired from {@code PermanentChoiceBattlefieldHandlerService.handleChampionCreature}
     *  right after the championed creature is exiled. Effects that target a player are routed through
     *  the {@code PermanentChoiceContext.ChampionedTriggerTarget} interaction. Used by Mistbind Clique
     *  ("When a Faerie is championed with this creature, tap all lands target player controls"). */
    ON_CHAMPIONED,
    /** Triggers whenever this creature saddles a Mount or crews a Vehicle during its controller's
     *  main phase. The Mount or Vehicle is stored as the non-targeting source permanent on the
     *  triggered ability's stack entry. Checked from {@code CrewCostHandler} as each creature is
     *  tapped to pay the power-based cost. */
    ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE,
    /** Triggers whenever a creature stations this Spacecraft. */
    ON_SELF_BECOMES_STATIONED,

    ON_CONTROLLER_COMMITS_CRIME,
    ON_OPPONENT_COMMITS_CRIME,
    GRAVEYARD_ON_CONTROLLER_COMMITS_CRIME,
    ON_SELF_BECOMES_PLOTTED,
    ON_ALLY_AURA_ATTACHED_TO_OPPONENT_NONLAND_PERMANENT,
    ON_EQUIPMENT_ATTACHED_TO_CREATURE,
    /** Triggers whenever this Vehicle becomes crewed for the first time each turn. */
    ON_SELF_BECOMES_CREWED,
    ON_SELF_BECOMES_SADDLED,
    ON_ALLY_CREATURE_MUTATES,
    ON_SELF_MUTATES
}
