package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Which of the last old slot collectors reads a triggered ability, now that the ability has a real trigger
 * (effect-slot-refactor.md step 13, family 11, run 11b: control change, attaching, crew, saddle and station, day and
 * night, Rooms and state triggers; Planechase reads its triggers directly). Each constant is named after the slot whose
 * collector it stands for and names the trigger its cards were converted to and the zone the collector reads them in.
 * The collectors in {@code TriggerCollectionService}, {@code CreatureControlService} and {@code StateTriggerService}
 * still decide when the ability triggers, how its effects group into stack objects and which event data it is bound
 * to, as the slot's did, until step 15 of the refactor replaces them with effects that read the event.
 * <p>
 * Every collector of these watches reads the abilities themselves ({@link Trigger#collectedAsAbility()}), through
 * {@link MiscAbilityReader}: a Room door, "becomes crewed / saddled for the first time each turn", "triggers only once
 * each turn" and a state trigger's game state come from the ability, and the legacy view gives none of them, so
 * {@link #effectsOf} and {@link #registrationsOf} throw.
 */
public enum MiscWatch implements TriggerWatch {
    /** "When you gain control of this from another player". */
    ON_SELF_BECOMES_CONTROLLED(Triggers.youGainControlOfThis(), Zone.BATTLEFIELD),
    /** "When you lose control of this". */
    ON_SELF_LOSES_CONTROL(Triggers.youLoseControlOfThis(), Zone.BATTLEFIELD),
    /** "Whenever an opponent gains control of a permanent you control". */
    ON_OPPONENT_GAINS_CONTROL_OF_YOUR_PERMANENT(Triggers.opponentGainsControlOfPermanentYouControl(),
            Zone.BATTLEFIELD),
    /** "Whenever an Aura becomes attached to this permanent", whoever controls the Aura. */
    ON_AURA_ATTACHED_TO_SELF(Triggers.auraBecomesAttachedToThis(), Zone.BATTLEFIELD),
    /** "Whenever an Aura you control becomes attached to a creature you control". */
    ON_ALLY_AURA_ATTACHED_TO_ALLY_CREATURE(Triggers.auraYouControlBecomesAttachedToCreatureYouControl(),
            Zone.BATTLEFIELD),
    /**
     * "Whenever an Aura you control becomes attached to a nonland permanent an opponent controls with mana value less
     * than or equal to that Aura's mana value".
     */
    ON_ALLY_AURA_ATTACHED_TO_OPPONENT_NONLAND_PERMANENT(Triggers.auraYouControlBecomesAttachedToNonlandPermanentOpponentControls(),
            Zone.BATTLEFIELD),
    /** "Whenever this Equipment becomes attached to a creature". */
    ON_EQUIPMENT_ATTACHED(Triggers.becomesAttachedToCreature(), Zone.BATTLEFIELD),
    /** "Whenever this creature crews a Vehicle". */
    ON_CREWS_VEHICLE(Triggers.crewsVehicle(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control crews a Vehicle", this creature included. */
    ON_ALLY_CREATURE_CREWS_VEHICLE(Triggers.creatureYouControlCrewsVehicle(), Zone.BATTLEFIELD),
    /** "Whenever this creature saddles a Mount or crews a Vehicle during your main phase". */
    ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE(Triggers.saddlesOrCrewsDuringMainPhase(), Zone.BATTLEFIELD),
    /** "Whenever a creature stations this Spacecraft". */
    ON_SELF_BECOMES_STATIONED(Triggers.becomesStationed(), Zone.BATTLEFIELD),
    /** "Whenever this Vehicle becomes crewed". */
    ON_SELF_BECOMES_CREWED(Triggers.becomesCrewed(), Zone.BATTLEFIELD),
    /** "Whenever this creature becomes saddled". */
    ON_SELF_BECOMES_SADDLED(Triggers.becomesSaddled(), Zone.BATTLEFIELD),
    /** "Whenever day becomes night or night becomes day". */
    ON_DAY_NIGHT_CHANGE(Triggers.dayBecomesNightOrNightBecomesDay(), Zone.BATTLEFIELD),
    /** "Whenever day becomes night or night becomes day", while this card is in your graveyard. */
    GRAVEYARD_ON_DAY_NIGHT_CHANGE(Triggers.dayBecomesNightOrNightBecomesDay(), Zone.GRAVEYARD),
    /** "Whenever you fully unlock a Room". */
    ON_ALLY_ROOM_FULLY_UNLOCKED(Triggers.youFullyUnlockRoom(), Zone.BATTLEFIELD),
    /** "Whenever you fully unlock a Room", while this card is in your graveyard. */
    GRAVEYARD_ON_ALLY_ROOM_FULLY_UNLOCKED(Triggers.youFullyUnlockRoom(), Zone.GRAVEYARD),
    /** "When you unlock this door". */
    ON_SELF_ROOM_DOOR_UNLOCKED(Triggers.doorUnlocked(), Zone.BATTLEFIELD),
    /** A state trigger (CR 603.8). */
    STATE_TRIGGERED(Triggers.state(), Zone.BATTLEFIELD);

    private final Trigger trigger;
    private final Zone zone;

    MiscWatch(Trigger trigger, Zone zone) {
        this.trigger = trigger;
        this.zone = zone;
    }

    @Override
    public Trigger trigger() {
        return trigger;
    }

    @Override
    public Zone zone() {
        return zone;
    }

    /** Throws: the collectors of a misc watch read its abilities themselves ({@link MiscAbilityReader}). */
    @Override
    public List<CardEffect> effectsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }

    /** Throws: the collectors of a misc watch read its abilities themselves ({@link MiscAbilityReader}). */
    @Override
    public List<EffectRegistration> registrationsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }
}
