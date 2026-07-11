package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * The operation to run when a {@link PendingInteraction.MultiPermanentChoice} is answered —
 * the multi-select analogue of {@link PermanentChoiceContext}. Begin sites pass the context
 * into {@code PlayerInputService.beginMultiPermanentChoice}, which snapshots it onto the
 * active record; the answer handler dispatches on it. Each record replaces the per-mechanic
 * {@code pending*} carry-over fields {@code GameData} used to hold for that operation, so the
 * state now exists only while its interaction is active and is copied for simulation with the
 * active record (records are immutable, shallow copy).
 */
public sealed interface MultiPermanentChoiceContext {

    /** Exile a permanent the damaged player controls (combat damage trigger). */
    record ExileDamagedPlayerControls() implements MultiPermanentChoiceContext {
    }

    /** "You may sacrifice [source]. If you do, destroy target creature that player controls." */
    record SacrificeSelfToDestroy(UUID sourcePermanentId) implements MultiPermanentChoiceContext {
    }

    /** Transform [source] and attach it to a creature the damaged player controls. */
    record TransformAndAttach(UUID sourcePermanentId) implements MultiPermanentChoiceContext {
    }

    /** The defending player sacrifices the chosen attacking creatures. */
    record SacrificeAttackingCreatures() implements MultiPermanentChoiceContext {
    }

    /**
     * Forced sacrifice pick ("target player sacrifices N" / "each player sacrifices N").
     * {@code sacrificingPlayerId} is the current chooser. For the each-player flow (CR 101.4:
     * all chosen permanents are sacrificed at the same time), {@code remainingChoosers} holds
     * the players still to choose in APNAP order and {@code accumulatedSacrificeIds} the ids
     * chosen so far (including auto-picks made at begin time); each answered pick re-begins
     * with the head of the remainder. For the direct single-player flow both lists are empty
     * and the chosen permanents are sacrificed immediately.
     */
    record ForcedSacrifice(UUID sacrificingPlayerId,
                           java.util.List<PendingForcedSacrifice> remainingChoosers,
                           java.util.List<UUID> accumulatedSacrificeIds)
            implements MultiPermanentChoiceContext {
    }

    /**
     * "Each player chooses a creature to keep, the rest are destroyed" (destroy-rest flow).
     * {@code remainingChoosers} and {@code protectedIds} advance across re-begins exactly as
     * in {@link ForcedSacrifice}; after the last chooser, every creature not in
     * {@code protectedIds} is destroyed. {@code sourceName} is kept for the completion log.
     */
    record DestroyRestChoice(java.util.List<PendingForcedSacrifice> remainingChoosers,
                             java.util.List<UUID> protectedIds, String sourceName)
            implements MultiPermanentChoiceContext {
    }

    /** Return the chosen permanents {@code targetPlayerId} controls to their owner's hand. */
    record CombatDamageBounce(UUID targetPlayerId) implements MultiPermanentChoiceContext {
    }

    /** Put an aim counter on each chosen permanent, then resume effect resolution. */
    record AimCounterPlacement() implements MultiPermanentChoiceContext {
    }

    /** Put {@code count} counters of {@code counterType} on the single chosen own permanent. */
    record OwnPermanentCounterPlacement(CounterType counterType, int count)
            implements MultiPermanentChoiceContext {
    }

    /** Put an awakening counter on each chosen land (they become 8/8 Elementals). */
    record AwakeningCounterPlacement() implements MultiPermanentChoiceContext {
    }

    /**
     * Proliferate. {@code remainingCount} includes the upcoming pick; each answered pick
     * re-begins with the decremented count (fresh-record-per-pick pattern) until it hits zero.
     */
    record Proliferate(int remainingCount) implements MultiPermanentChoiceContext {
    }

    /** Tap the chosen subtype permanents to boost [source] and damage the defender (Myr Battlesphere). */
    record TapSubtypeBoost(UUID sourcePermanentId) implements MultiPermanentChoiceContext {
    }

    /**
     * {@code targetPlayerId} chose one creature to keep able to block; every other creature they
     * control can't block this turn (Goblin War Cry).
     */
    record ChooseCreatureRestCantBlock(UUID targetPlayerId) implements MultiPermanentChoiceContext {
    }

    /**
     * Tap each chosen untapped creature the controller controls, then the controller gains
     * {@code lifePerCreature} life per creature tapped this way (Harmony of Nature).
     */
    record TapCreaturesGainLife(int lifePerCreature) implements MultiPermanentChoiceContext {
    }
}
