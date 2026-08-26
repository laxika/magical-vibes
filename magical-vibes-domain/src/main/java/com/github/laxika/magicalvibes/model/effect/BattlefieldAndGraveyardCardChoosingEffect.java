package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for effects whose targets are chosen from the battlefield <em>and</em>
 * graveyards in one selection — "up to three other target creatures from the battlefield and/or
 * creature cards from graveyards" (Angel of Serenity). No single {@link TargetSpec} can describe a
 * choice that spans two zones, and the engine's battlefield and graveyard targeting pipelines are
 * otherwise separate, so the pool is assembled as one card list and the chosen ids ride on the
 * triggered ability's {@code targetCardIds}.
 *
 * <p>Descriptive only: it states facts drawn from the record's existing components. Trigger
 * collectors read it to route the ability to {@code GraveyardTargetingService}'s mixed-zone
 * selection instead of pushing the trigger straight onto the stack, without naming a concrete
 * effect type.
 */
public interface BattlefieldAndGraveyardCardChoosingEffect extends CardEffect {

    /** The maximum number of cards the controller may choose across both zones ("up to N"). */
    int mixedZoneMaxTargets();

    /** Maximum number for a choice whose cap is the announced X value. */
    default int mixedZoneMaxTargets(int xValue) {
        return mixedZoneMaxTargets();
    }
}
