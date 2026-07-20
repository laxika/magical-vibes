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

    /** Destroy a permanent the damaged player controls (mandatory combat damage trigger, e.g. Deus of Calamity). */
    record DestroyDamagedPlayerControls(String sourceName) implements MultiPermanentChoiceContext {
    }

    /**
     * Untap the single chosen permanent (any battlefield), for a triggered ability with no cast-time
     * target — e.g. Initiate's Companion's "untap target creature or land". The choosable permanents
     * were narrowed at begin time by the effect's predicate.
     */
    record UntapChosenPermanent(String sourceName) implements MultiPermanentChoiceContext {
    }

    /** Sacrifice a permanent the damaged player controls (mandatory combat damage trigger, e.g. Ashling, the Extinguisher). */
    record SacrificeDamagedPlayerControls(String sourceName) implements MultiPermanentChoiceContext {
    }

    /** "You may sacrifice [source]. If you do, destroy target creature that player controls." */
    record SacrificeSelfToDestroy(UUID sourcePermanentId, boolean cannotBeRegenerated) implements MultiPermanentChoiceContext {
    }

    /**
     * "Gain control of the chosen land the defending player controls for as long as you control
     * [source]. If you do, [source] assigns no combat damage this turn." (Orcish Squatters.)
     * {@code sourcePermanentId} is the attacking source creature.
     */
    record GainControlOfLandAndAssignNoCombatDamage(UUID sourcePermanentId) implements MultiPermanentChoiceContext {
    }

    /** Transform [source] and attach it to a creature the damaged player controls. */
    record TransformAndAttach(UUID sourcePermanentId) implements MultiPermanentChoiceContext {
    }

    /** The defending player sacrifices the chosen attacking creatures. */
    record SacrificeAttackingCreatures() implements MultiPermanentChoiceContext {
    }

    /** The controller exiles the chosen attacking creatures (Resounding Silence cycling trigger). */
    record ExileAttackingCreatures() implements MultiPermanentChoiceContext {
    }

    /** The controller returns the chosen permanents to their owners' hands (Resounding Wave cycling trigger). */
    record ReturnTargetPermanentsToHand() implements MultiPermanentChoiceContext {
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

    /**
     * Forced destroy pick ("you/target player destroys N permanents you control"). The
     * {@code destroyingPlayerId} is the chooser; the chosen permanents are destroyed (regeneration
     * and indestructible apply) and then effect resolution resumes. {@code sourceName} is used for
     * the destruction log. Used by {@code PlayerDestroysPermanentsEffect} (e.g. Burning of Xinye).
     */
    record ForcedDestroy(UUID destroyingPlayerId, String sourceName)
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

    /**
     * Sacrifice the chosen lands, then search the library for up to that many land cards and put
     * them onto the battlefield tapped, then shuffle (Scapeshift). The number of lands searched
     * for equals the number of lands sacrificed.
     */
    record SacrificeLandsSearchLandsToBattlefieldTapped() implements MultiPermanentChoiceContext {
    }

    /**
     * Sacrifice the chosen permanents, then the controller draws a card for each one actually
     * sacrificed (Reprocess). The draw count equals the number of permanents sacrificed.
     */
    record SacrificePermanentsDrawPerSacrificed() implements MultiPermanentChoiceContext {
    }

    /**
     * Clarion Ultimatum: the controller chose up to five different permanents they control. For
     * each chosen permanent, the controller may then search their library for a card with the same
     * name and put it onto the battlefield tapped; the same-name searches run one per chosen
     * permanent (queued via {@link LibrarySearchFollowUp#remainingSameNamePicks()}), then shuffle.
     */
    record ChooseFivePermanentsSearchSameNameToBattlefieldTapped() implements MultiPermanentChoiceContext {
    }

    /**
     * Devour (CR 702.82): the entering creature's controller chose which of their other creatures to
     * sacrifice as it enters. The chosen creatures are sacrificed, the entering permanent receives
     * {@code multiplier} times that many +1/+1 counters and records the devoured count, then the
     * creature's ETB triggers proceed. Carries the entry context needed to resume
     * {@code processCreatureETBEffects} for the discard trigger.
     */
    record DevourSacrifice(UUID enteringPermanentId, int multiplier, UUID controllerId, Card card,
                           UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked)
            implements MultiPermanentChoiceContext {
    }

    /**
     * Magnetic Mountain: the acting player ({@code actingPlayerId}, the player whose upkeep it is)
     * chose any number of their tapped blue creatures (up to what they can afford). They pay
     * {@code manaPerCreature} for each chosen creature from their mana pool, then those creatures
     * untap. The choice was already capped at begin time by the mana available, so payment always
     * succeeds; the empty selection means "untap none".
     */
    record PayManaPerCreatureUntap(UUID actingPlayerId, int manaPerCreature)
            implements MultiPermanentChoiceContext {
    }

    /**
     * Tetravus second upkeep trigger: exile the chosen tokens (each of which must be a token
     * {@code sourcePermanentId} created, tracked in {@code GameData.tetravusCreatedTokens}), then put
     * that many +1/+1 counters on the source.
     */
    record ExileTetraviteTokensPutCountersOnSource(UUID sourcePermanentId)
            implements MultiPermanentChoiceContext {
    }

    /**
     * Static Orb / Stoic Angel: the active player chose up to the cap of the permanents matching
     * {@code filter} that would otherwise untap; only those (plus any permanents the filter excludes)
     * untap this step. A {@code null} filter means all permanents count against the cap (Static Orb).
     * The untap-step bookkeeping and turn advance then resume exactly as they would have without the
     * restriction.
     */
    record StaticOrbUntap(UUID activePlayerId,
                          com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter)
            implements MultiPermanentChoiceContext {
    }
}
