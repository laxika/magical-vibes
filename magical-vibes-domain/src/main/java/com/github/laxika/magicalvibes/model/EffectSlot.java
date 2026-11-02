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
    /** State-triggered abilities (MTG rule 603.8). Checked after SBAs; fire once onto the
     *  stack and don't retrigger while the ability is already on the stack. */
    STATE_TRIGGERED,
    /** Triggers whenever this creature saddles a Mount or crews a Vehicle during its controller's
     *  main phase. The Mount or Vehicle is stored as the non-targeting source permanent on the
     *  triggered ability's stack entry. Checked from {@code CrewCostHandler} as each creature is
     *  tapped to pay the power-based cost. */
    ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE,
    /** Triggers whenever a creature stations this Spacecraft. */
    ON_SELF_BECOMES_STATIONED,

    ON_ALLY_AURA_ATTACHED_TO_OPPONENT_NONLAND_PERMANENT,
    ON_EQUIPMENT_ATTACHED_TO_CREATURE,
    /** Triggers whenever this Vehicle becomes crewed for the first time each turn. */
    ON_SELF_BECOMES_CREWED,
    ON_SELF_BECOMES_SADDLED
}
