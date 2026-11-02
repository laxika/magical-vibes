package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Which of the old tap and untap slot collectors reads a triggered ability, now that the ability has a real trigger
 * (effect-slot-refactor.md step 13, family 8). Each constant is named after the slot whose collector it stands for and
 * names the trigger its cards were converted to and the zone the collector reads them in. The collectors in
 * {@code TriggerCollectionService} still decide when the ability triggers, how its effects group into stack objects,
 * which wrappers filter it and which event data it is bound to, as the slot's did, until steps 14 and 15 of the
 * refactor replace them with trigger options and effects that read the event.
 */
public enum TapWatch implements TriggerWatch {
    /** "Whenever you tap this permanent for mana", from the mana-ability tap path only. */
    ON_SELF_TAPPED_FOR_MANA(Triggers.tappedForMana(), Zone.BATTLEFIELD),
    /** "Whenever a player taps a land for mana"; the effect class narrows the player and the land. */
    ON_ANY_PLAYER_TAPS_LAND(Triggers.playerTapsLandForMana(), Zone.BATTLEFIELD),
    /** "Whenever a player taps a permanent for mana". */
    ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA(Triggers.playerTapsPermanentForMana(), Zone.BATTLEFIELD),
    /** "Whenever you tap a creature for mana". */
    ON_CONTROLLER_TAPS_CREATURE_FOR_MANA(Triggers.youTapCreatureForMana(), Zone.BATTLEFIELD),
    /** "Whenever you tap a nonland permanent for mana". */
    ON_CONTROLLER_TAPS_NONLAND_PERMANENT_FOR_MANA(Triggers.youTapNonlandPermanentForMana(), Zone.BATTLEFIELD),
    /** "Whenever you tap an artifact token for mana". */
    ON_CONTROLLER_TAPS_ARTIFACT_TOKEN_FOR_MANA(Triggers.youTapArtifactTokenForMana(), Zone.BATTLEFIELD),
    /** "Whenever enchanted permanent becomes tapped", read on every permanent attached to the tapped one. */
    ON_ENCHANTED_PERMANENT_TAPPED(Triggers.enchantedPermanentBecomesTapped(), Zone.BATTLEFIELD),
    /** "Whenever a permanent you control becomes tapped". */
    ON_ALLY_PERMANENT_BECOMES_TAPPED(Triggers.permanentYouControlBecomesTapped(), Zone.BATTLEFIELD),
    /** "Whenever this creature becomes tapped to pay a teamwork cost". */
    ON_SELF_TAPPED_TO_PAY_TEAMWORK_COST(Triggers.tappedToPayTeamworkCost(), Zone.BATTLEFIELD),
    /** "Whenever this permanent becomes untapped". */
    ON_SELF_BECOMES_UNTAPPED(Triggers.becomesUntapped(), Zone.BATTLEFIELD),
    /** "Whenever a permanent you control becomes untapped". */
    ON_ALLY_PERMANENT_BECOMES_UNTAPPED(Triggers.permanentYouControlBecomesUntapped(), Zone.BATTLEFIELD),
    /** "Whenever you untap one or more permanents during your untap step", once for each untap step. */
    ON_CONTROLLER_UNTAPS_DURING_UNTAP_STEP(Triggers.youUntapPermanentsDuringUntapStep(), Zone.BATTLEFIELD),
    /** "Whenever a permanent becomes untapped". */
    ON_ANY_PERMANENT_BECOMES_UNTAPPED(Triggers.permanentBecomesUntapped(), Zone.BATTLEFIELD),
    /** "Whenever a permanent an opponent controls becomes tapped". */
    ON_OPPONENT_PERMANENT_BECOMES_TAPPED(Triggers.permanentOpponentControlsBecomesTapped(), Zone.BATTLEFIELD),
    /** "Whenever you tap a permanent an opponent controls". */
    ON_CONTROLLER_TAPS_OPPONENT_PERMANENT(Triggers.youTapPermanentOpponentControls(), Zone.BATTLEFIELD),
    /** "Whenever a creature becomes tapped". */
    ON_ANY_CREATURE_BECOMES_TAPPED(Triggers.creatureBecomesTapped(), Zone.BATTLEFIELD);

    private final Trigger trigger;
    private final Zone zone;

    TapWatch(Trigger trigger, Zone zone) {
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
}
