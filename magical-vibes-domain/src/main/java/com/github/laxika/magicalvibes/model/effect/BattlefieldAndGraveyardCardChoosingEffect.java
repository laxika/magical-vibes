package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability interface for effects whose targets are chosen from the battlefield <em>and</em>
 * graveyards in one selection — "up to three other target creatures from the battlefield and/or
 * creature cards from graveyards" (Angel of Serenity). No single {@link TargetSpec} can describe a
 * choice that spans two zones, and the engine's battlefield and graveyard targeting pipelines are
 * otherwise separate, so the pool is assembled as one card list and the chosen ids ride on the
 * triggered ability's {@code targetCardIds}.
 *
 * <p>Trigger collectors read it to route the ability to
 * {@code GraveyardTargetingService}'s mixed-zone selection instead of pushing the trigger straight
 * onto the stack. Implementations may also declare zone-specific limits and predicates without
 * naming a concrete effect type in the targeting pipeline.
 */
public interface BattlefieldAndGraveyardCardChoosingEffect extends CardEffect {

    /** The maximum number of cards the controller may choose across both zones ("up to N"). */
    int mixedZoneMaxTargets();

    /** Maximum number for a choice whose cap is the announced X value. */
    default int mixedZoneMaxTargets(int xValue) {
        return mixedZoneMaxTargets();
    }
    /** The maximum number of battlefield permanents in the mixed-zone selection. */
    default int mixedZoneMaxBattlefieldTargets() {
        return mixedZoneMaxTargets();
    }

    /** The maximum number of graveyard cards in the mixed-zone selection. */
    default int mixedZoneMaxGraveyardTargets() {
        return mixedZoneMaxTargets();
    }

    /** The battlefield filter, or {@code null} for the legacy creature-only choice. */
    default PermanentPredicate mixedZoneBattlefieldPredicate() {
        return null;
    }

    /** The graveyard-card filter, or {@code null} for the legacy creature-card choice. */
    default CardPredicate mixedZoneGraveyardPredicate() {
        return null;
    }

    /** Whether the source permanent is excluded from the battlefield choice. */
    default boolean mixedZoneExcludesSourcePermanent() {
        return true;
    }

    /** The prompt suffix describing the mixed-zone choices. */
    default String mixedZoneChoiceDescription(int maxTargets) {
        return "target creature" + (maxTargets != 1 ? "s" : "")
                + " on the battlefield and/or creature card"
                + (maxTargets != 1 ? "s" : "") + " in graveyards to exile.";
    }
}
