package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;

/**
 * What a triggered ability triggers on: a game event and the filters it must match, or a game
 * state for a state trigger (CR 603.8). Triggers are pure data, like predicates: the engine, not
 * the trigger, decides whether something that happens in the game matches it.
 */
public sealed interface Trigger permits AbilityTrigger, AttachTrigger, ChapterTrigger, CombatTrigger,
        ControlChangeTrigger, CounterTrigger, DamageTrigger, DayNightTrigger, FinalChapterTrigger, LandPlayTrigger,
        LegacySlotTrigger, LibraryTrigger, LifeTrigger, ObjectActionTrigger, PermanentChangeTrigger, PlanarTrigger,
        PlayerActionTrigger, SpellTrigger, StateTrigger, StepTrigger, TapTrigger, TargetTrigger, ZoneChangeTrigger {

    /**
     * The zones a trigger condition on this trigger functions from when the card doesn't say
     * otherwise: the battlefield (CR 113.6). A trigger that can't trigger from the battlefield
     * returns the zones it can trigger from instead (CR 113.6k).
     */
    default AbilityZones defaultZones() {
        return AbilityZones.BATTLEFIELD;
    }

    /**
     * The trigger the old collectors read this one's abilities by (a {@code TriggerWatch}'s trigger): this trigger
     * without the filters they still judge from the wrappers {@code TriggeredAbility.legacyEffects()} gives (a filter,
     * a Room door, a state trigger's game state), and for some the trigger of the event whose collector reads it (a
     * counter or tap trigger, "becomes crewed for the first time each turn"). A trigger without such filters is its own
     * key.
     */
    default Trigger watchKey() {
        return this;
    }

    /**
     * Whether every collector of this trigger reads its abilities themselves (the trigger's filters, the ability's
     * options and its effects), so the legacy view ({@code TriggeredAbility.legacyEffects()}) gives none of them: the
     * spell, land-play and activation triggers (step 13's family 2), whose collectors effect-slot-refactor.md step 14
     * moved off the legacy view in runs 16b to 16d, the "at the beginning of [step or phase]" triggers (family 1,
     * run 16e), the combat triggers (family 3, run 16f), the damage triggers (family 4, run 16g), the counter
     * triggers (family 5, run 16h) and the becomes-the-target triggers (family 6, run 16i).
     */
    default boolean collectedAsAbility() {
        return false;
    }
}
