package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WormsOfTheEarthEffect;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;
import java.util.Map;
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

    record RemoveCounterFromChosenPermanents(StackEntry resolvingEntry, CounterType counterType,
                                             PermanentPredicate permanentFilter)
            implements MultiPermanentChoiceContext {
    }

    /** The activating player chooses one or more other artifacts to exile as an ability cost. */
    record ActivatedAbilityExileArtifactsCost(UUID playerId, UUID sourcePermanentId, int abilityIndex,
                                               int xValue, UUID targetId, Zone targetZone,
                                               List<UUID> targetIds, Map<UUID, Integer> damageAssignments,
                                               ActivatedAbility ability, Permanent sourcePermanentSnapshot)
            implements MultiPermanentChoiceContext {

        public ActivatedAbilityExileArtifactsCost {
            targetIds = targetIds == null ? List.of() : List.copyOf(targetIds);
            damageAssignments = damageAssignments == null ? Map.of() : Map.copyOf(damageAssignments);
        }
    }

    record SagaChapterCounterDistribution(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, String chapterName, CounterType counterType,
                                          int total) implements MultiPermanentChoiceContext {

        public SagaChapterCounterDistribution {
            effects = List.copyOf(effects);
        }
    }

    /** The controller selected any number of player targets for a Saga chapter. */
    record SagaChapterTargetSelection(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                      UUID sourcePermanentId, String chapterName, int minTargets)
            implements MultiPermanentChoiceContext {

        public SagaChapterTargetSelection {
            effects = List.copyOf(effects);
        }
    }

    /** Selects zero or more players for an upkeep trigger. */
    record UpkeepAnyNumberPlayerTargets(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                        UUID sourcePermanentId) implements MultiPermanentChoiceContext {

        public UpkeepAnyNumberPlayerTargets {
            effects = List.copyOf(effects);
        }
    }

    /** Selects zero or one permanent for an optional upkeep target. */
    record UpkeepOptionalPermanentTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                         UUID sourcePermanentId) implements MultiPermanentChoiceContext {

        public UpkeepOptionalPermanentTarget {
            effects = List.copyOf(effects);
        }
    }

    record CounterDistribution(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                UUID sourcePermanentId, CounterType counterType, int total)
            implements MultiPermanentChoiceContext {

        public CounterDistribution {
            effects = List.copyOf(effects);
        }
    }

    /** Exile a permanent the damaged player controls (combat damage trigger). */
    record ExileDamagedPlayerControls() implements MultiPermanentChoiceContext {
    }

    /** Deal damage to a creature the damaged player controls (combat damage trigger). */
    record DealDamageToDamagedPlayerControls(StackEntry damageEntry, DynamicAmount damage)
            implements MultiPermanentChoiceContext {

        public DealDamageToDamagedPlayerControls(StackEntry damageEntry, int damage) {
            this(damageEntry, new com.github.laxika.magicalvibes.model.amount.Fixed(damage));
        }
    }

    record DestroyUpToOneAttachedPermanent(UUID targetCreatureId, PermanentPredicate attachedFilter,
                                           UUID sourceCardId, UUID sourceControllerId,
                                           String sourceCardName) implements MultiPermanentChoiceContext {
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

    /** Choose a creature or planeswalker to receive redirected damage for the rest of the turn. */
    record RedirectDamageToChosenPermanent(UUID protectedPlayerId, String sourceName)
            implements MultiPermanentChoiceContext {
    }

    /**
     * Tap the single chosen permanent (any battlefield), for a triggered ability with no cast-time
     * target — e.g. Thalakos Dreamsower's "tap target creature". When
     * {@code preventUntapWhileSourceTapped} is set, the chosen permanent is also untap-locked for as
     * long as {@code sourcePermanentId} remains tapped.
     */
    record TapChosenPermanent(String sourceName, UUID sourcePermanentId,
                              boolean preventUntapWhileSourceTapped,
                              boolean preventUntapWhileSourceOnBattlefield,
                              boolean skipNextUntap) implements MultiPermanentChoiceContext {

        public TapChosenPermanent(String sourceName, UUID sourcePermanentId,
                                  boolean preventUntapWhileSourceTapped) {
            this(sourceName, sourcePermanentId, preventUntapWhileSourceTapped, false, false);
        }
    }

    /** Sacrifice a permanent the damaged player controls (mandatory combat damage trigger, e.g. Ashling, the Extinguisher). */
    record SacrificeDamagedPlayerControls(String sourceName) implements MultiPermanentChoiceContext {
    }

    /** "You may sacrifice [source]. If you do, destroy target creature that player controls." */
    record SacrificeSelfToDestroy(UUID sourcePermanentId, boolean cannotBeRegenerated) implements MultiPermanentChoiceContext {
    }

    /**
     * "Gain control of the chosen permanent the defending player controls[ for as long as you
     * control [source]]. If you do, [source] assigns no combat damage this turn." (Orcish Squatters
     * — lands; Kukemssa Pirates — artifacts.) {@code sourcePermanentId} is the attacking source
     * creature, {@code duration} how long control is kept and {@code choiceNoun} the noun used in
     * the game log.
     */
    record GainControlOfPermanentAndAssignNoCombatDamage(UUID sourcePermanentId, ControlDuration duration,
                                                         String choiceNoun) implements MultiPermanentChoiceContext {
    }

    /**
     * "Destroy the chosen permanent the defending player controls and [source] assigns no combat
     * damage this turn." (Goblin Vandal — artifacts.) {@code sourcePermanentId} is the attacking
     * source creature and {@code choiceNoun} the noun used in the game log.
     */
    record DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamage(UUID sourcePermanentId,
                                                                          String choiceNoun)
            implements MultiPermanentChoiceContext {
    }

    /** Transform [source] and attach it to a creature the damaged player controls. */
    record TransformAndAttach(UUID sourcePermanentId) implements MultiPermanentChoiceContext {
    }

    /** The controller transforms any number of matching permanents. */
    record TransformAnyNumber(StackEntry resolvingEntry, PermanentPredicate filter)
            implements MultiPermanentChoiceContext {
    }

    /** The controller selected Equipment they control to attach to the targeted creature. */
    record AttachAnyNumberOfControlledEquipmentToTargetCreature(UUID targetCreatureId)
            implements MultiPermanentChoiceContext {
    }

    /** The defending player sacrifices the chosen attacking creatures. */
    record SacrificeAttackingCreatures() implements MultiPermanentChoiceContext {
    }

    /** The controller exiles the chosen attacking creatures (Resounding Silence cycling trigger). */
    record ExileAttackingCreatures() implements MultiPermanentChoiceContext {
    }

    /**
     * Aetherspouts: the current owner selects the attacking creatures that go on top; the other
     * attacking creatures in that owner's group go on the bottom. The remaining and accumulated
     * IDs carry the resolution through one owner at a time.
     */
    record PutAttackingCreaturesOnLibrary(List<UUID> remainingCreatureIds, List<UUID> topCreatureIds,
                                          List<UUID> bottomCreatureIds, String sourceCardName)
            implements MultiPermanentChoiceContext {

        public PutAttackingCreaturesOnLibrary {
            remainingCreatureIds = List.copyOf(remainingCreatureIds);
            topCreatureIds = List.copyOf(topCreatureIds);
            bottomCreatureIds = List.copyOf(bottomCreatureIds);
        }
    }

    /** Each owner chooses the order of their matching permanents on top of their libraries. */
    record PutPermanentsOnTopOfOwnersLibraries(List<UUID> remainingPermanentIds,
                                               List<UUID> orderedPermanentIds, String sourceCardName)
            implements MultiPermanentChoiceContext {

        public PutPermanentsOnTopOfOwnersLibraries {
            remainingPermanentIds = List.copyOf(remainingPermanentIds);
            orderedPermanentIds = List.copyOf(orderedPermanentIds);
        }
    }

    /**
     * The controller destroys the chosen creatures an opponent controls (Fatal Lore's second mode).
     * {@code sourceName} names the destroying source in the game log and {@code cannotBeRegenerated}
     * carries the "they can't be regenerated" clause.
     */
    record DestroyCreaturesOpponentControls(String sourceName, boolean cannotBeRegenerated)
            implements MultiPermanentChoiceContext {
    }

    /**
     * The controller taps the chosen permanents a target player controls (Yosei, the Morning Star's
     * "Tap up to five target permanents that player controls"). {@code sourceName} names the source
     * in the game log.
     */
    record TapChosenPermanents(String sourceName) implements MultiPermanentChoiceContext {
    }

    /** Tap exactly the required number of matching permanents chosen by the affected player. */
    record TapPermanentsForAmount(String sourceName, int requiredCount) implements MultiPermanentChoiceContext {
    }

    /**
     * The controller untaps the chosen permanents (Rewind's "Untap up to four lands").
     * {@code sourceName} names the source in the game log.
     */
    record UntapChosenPermanents(String sourceName) implements MultiPermanentChoiceContext {
    }

    /** The affected player chooses exactly the required number of permanents to untap. */
    record UntapPermanentsForAmount(String sourceName, int requiredCount)
            implements MultiPermanentChoiceContext {
    }

    /** The controller returns the chosen permanents to their owners' hands (Resounding Wave cycling trigger). */
    record ReturnTargetPermanentsToHand() implements MultiPermanentChoiceContext {
    }

    /** Return the chosen permanents and record the number returned on the resolving stack entry. */
    record ReturnAnyNumberAndRecordCount(StackEntry resolvingEntry) implements MultiPermanentChoiceContext {
    }

    /** Return exactly the required number of matching permanents, or all available if fewer exist. */
    record ReturnNControlledPermanentsToHand(
            com.github.laxika.magicalvibes.model.effect.ReturnNControlledPermanentsToHandEffect effect)
            implements MultiPermanentChoiceContext {
    }

    /** Resolve one choice in a repeated immediate controller-creature flicker. */
    record FlickerAnyNumber(StackEntry resolvingEntry,
                            com.github.laxika.magicalvibes.model.effect.FlickerEffect effect,
                            int remainingIterations) implements MultiPermanentChoiceContext {
    }

    /** Exile the selected face-up creatures with disguise and cloak them. */
    record RecloakDisguisedCreatures(StackEntry resolvingEntry) implements MultiPermanentChoiceContext {
    }

    /** Choose up to one creature that saddled the source this turn, then flicker the source and it. */
    record ExileSelfAndSaddledCreature(StackEntry resolvingEntry) implements MultiPermanentChoiceContext {
    }

    /** Choose a saddler, create its tapped and attacking token copy, and repeat as needed. */
    record CreateTokenCopiesOfSaddledCreature(StackEntry resolvingEntry, int remainingIterations)
            implements MultiPermanentChoiceContext {
    }

    /**
     * Forced sacrifice pick ("target player sacrifices N" / "each player sacrifices N").
     * {@code sacrificingPlayerId} is the current chooser. For the each-player flow (CR 101.4:
     * all chosen permanents are sacrificed at the same time), {@code remainingChoosers} holds
     * the players still to choose in APNAP order and {@code accumulatedSacrificeIds} the ids
     * chosen so far (including auto-picks made at begin time); each answered pick re-begins
     * with the head of the remainder. For the direct single-player flow both lists are empty
     * and the chosen permanents are sacrificed immediately. {@code recordSacrificedCount} carries
     * the actual number sacrificed back to the parked stack entry for a following effect.
     */
    record ForcedSacrifice(UUID sacrificingPlayerId,
                           java.util.List<PendingForcedSacrifice> remainingChoosers,
                           java.util.List<UUID> accumulatedSacrificeIds,
                           boolean simultaneousFlow,
                           boolean recordSacrificedCount)
            implements MultiPermanentChoiceContext {

        public ForcedSacrifice(UUID sacrificingPlayerId,
                               java.util.List<PendingForcedSacrifice> remainingChoosers,
                               java.util.List<UUID> accumulatedSacrificeIds) {
            this(sacrificingPlayerId, remainingChoosers, accumulatedSacrificeIds, false, false);
        }

        public ForcedSacrifice(UUID sacrificingPlayerId,
                               java.util.List<PendingForcedSacrifice> remainingChoosers,
                               java.util.List<UUID> accumulatedSacrificeIds,
                               boolean simultaneousFlow) {
            this(sacrificingPlayerId, remainingChoosers, accumulatedSacrificeIds, simultaneousFlow, false);
        }
    }

    /** Worms of the Earth: choose exactly two lands to sacrifice for its upkeep choice. */
    record WormsOfTheEarthSacrificeLands(UUID playerId, Card sourceCard, WormsOfTheEarthEffect effect)
            implements MultiPermanentChoiceContext {
    }

    /** The target player chooses a creature and a planeswalker to sacrifice when possible. */
    record TargetPlayerSacrificesCreatureAndPlaneswalker(UUID targetPlayerId,
                                                          List<UUID> creatureIds,
                                                          List<UUID> planeswalkerIds,
                                                          int requiredCount,
                                                          String sourceName)
            implements MultiPermanentChoiceContext {
        public TargetPlayerSacrificesCreatureAndPlaneswalker {
            creatureIds = List.copyOf(creatureIds);
            planeswalkerIds = List.copyOf(planeswalkerIds);
        }
    }

    /** Each opponent chooses an artifact creature and a nonartifact creature to sacrifice. */
    record EachOpponentSacrificesArtifactAndNonartifactCreature(List<UUID> playerIds,
                                                                  int playerIndex,
                                                                  List<UUID> accumulatedSacrificeIds,
                                                                  List<UUID> artifactCreatureIds,
                                                                  List<UUID> nonartifactCreatureIds,
                                                                  int requiredCount,
                                                                  UUID sourceControllerId,
                                                                  String sourceName)
            implements MultiPermanentChoiceContext {
        public EachOpponentSacrificesArtifactAndNonartifactCreature {
            playerIds = List.copyOf(playerIds);
            accumulatedSacrificeIds = List.copyOf(accumulatedSacrificeIds);
            artifactCreatureIds = List.copyOf(artifactCreatureIds);
            nonartifactCreatureIds = List.copyOf(nonartifactCreatureIds);
        }
    }

    /**
     * Desolation-style forced sacrifice: same APNAP simultaneous pick as {@link ForcedSacrifice},
     * then after all sacrifices the source deals {@code damageAmount} to each player who
     * sacrificed a permanent matching {@code subtype} this way. {@code damageEntry} is the
     * resolving stack entry (snapshotted for attribution after the interaction parks).
     */
    record ForcedSacrificeThenDamageIfSubtype(UUID sacrificingPlayerId,
                                              java.util.List<PendingForcedSacrifice> remainingChoosers,
                                              java.util.List<UUID> accumulatedSacrificeIds,
                                              CardSubtype subtype,
                                              int damageAmount,
                                              StackEntry damageEntry)
            implements MultiPermanentChoiceContext {
    }

    /**
     * "Choose a matching permanent to keep, the rest are destroyed" (destroy-rest flow).
     * {@code remainingChoosers} and {@code protectedIds} advance across re-begins exactly as
     * in {@link ForcedSacrifice}; after the last chooser, every matching permanent not in
     * {@code protectedIds} is destroyed. {@code sourceName} is kept for the completion log.
     */
    record DestroyRestChoice(java.util.List<PendingForcedSacrifice> remainingChoosers,
                             java.util.List<UUID> protectedIds, String sourceName,
                             com.github.laxika.magicalvibes.model.filter.PermanentPredicate destructionFilter,
                             String choicePrompt, boolean requiresChoice)
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

    /**
     * Forced return-to-hand pick ("sacrifice [source] unless you return N matching permanents to
     * their owner's hand"). Direct single-player flow — chosen permanents bounce immediately.
     */
    record ForcedReturnToHand(UUID returningPlayerId) implements MultiPermanentChoiceContext {
    }

    /** Each player chooses one creature to return to its owner's hand in active-player order. */
    record EachPlayerReturnsCreature(List<UUID> remainingPlayerIds, List<UUID> chosenIds,
                                     String sourceName) implements MultiPermanentChoiceContext {

        public EachPlayerReturnsCreature {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            chosenIds = List.copyOf(chosenIds);
        }
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

    /** Put counters on the chosen permanent and attribute the placement to the choosing player. */
    record OwnPermanentCounterPlacementByPlayer(CounterType counterType, int count, UUID placingPlayerId)
            implements MultiPermanentChoiceContext {
    }

    /** Put one counter on an opponent's creature for each remaining cumulative-upkeep payment. */
    record OpponentCreatureCounterPlacement(CounterType counterType, int remainingCount,
                                             UUID placingPlayerId) implements MultiPermanentChoiceContext {
    }

    /** Put counters on the chosen permanent and remember it for a following effect. */
    record OwnPermanentCounterPlacementWithChosenReference(CounterType counterType, int count,
                                                            boolean recordPlacement)
            implements MultiPermanentChoiceContext {

        public OwnPermanentCounterPlacementWithChosenReference(CounterType counterType, int count) {
            this(counterType, count, false);
        }
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

    /** Tap the chosen permanents to boost the source by the number tapped (Siege Striker). */
    record TapAnyNumberBoostSelf(UUID sourcePermanentId, int powerPerPermanent, int toughnessPerPermanent)
            implements MultiPermanentChoiceContext {
    }

    /** The controller chooses any number of untapped permanents to tap. */
    record TapAnyNumberPermanents() implements MultiPermanentChoiceContext {
    }

    /**
     * {@code targetPlayerId} chose one creature to keep able to block; every other creature they
     * control can't block this turn (Goblin War Cry).
     */
    record ChooseCreatureRestCantBlock(UUID targetPlayerId) implements MultiPermanentChoiceContext {
    }

    /**
     * Oracle en-Vec: {@code targetPlayerId} chose any number of creatures they control. The chosen
     * set is recorded in {@code GameData.chosenAttackersNextTurn} and takes effect when that
     * player's next turn begins — the chosen creatures attack if able, every other creature can't
     * attack, and each chosen creature that didn't attack is destroyed at that turn's end step.
     */
    record ChooseCreaturesToAttackNextTurn(UUID targetPlayerId) implements MultiPermanentChoiceContext {
    }

    /** The controller chooses equal numbers of creatures from two players for Cultural Exchange. */
    record CulturalExchange(Card sourceCard, UUID chooserId, UUID firstPlayerId, UUID secondPlayerId,
                            List<UUID> firstChosenIds, boolean firstSelection) implements MultiPermanentChoiceContext {

        public CulturalExchange {
            firstChosenIds = List.copyOf(firstChosenIds);
        }

    }

    /**
     * Tap each chosen untapped creature the controller controls, then the controller gains
     * {@code lifePerCreature} life per creature tapped this way (Harmony of Nature).
     */
    record TapCreaturesGainLife(int lifePerCreature) implements MultiPermanentChoiceContext {
    }

    /** Tap each chosen untapped creature, then boost the source by +1/+1 per creature tapped. */
    record TapCreaturesBoostSelf(UUID sourcePermanentId) implements MultiPermanentChoiceContext {
    }

    /** Tap exactly N other creatures, or decline, then make the source unblockable. */
    record TapOtherCreaturesForUnblockable(UUID sourcePermanentId, int requiredCount)
            implements MultiPermanentChoiceContext {
    }

    /** Raiding Party's current player chooses any number of untapped white creatures to tap. */
    record RaidingPartyTapChoice(List<UUID> playerIds, int playerIndex, List<Integer> tappedCounts,
                                 String sourceName) implements MultiPermanentChoiceContext {

        public RaidingPartyTapChoice {
            playerIds = List.copyOf(playerIds);
            tappedCounts = List.copyOf(tappedCounts);
        }
    }

    /** Raiding Party's current player chooses Plains to preserve from the final destruction. */
    record RaidingPartyPlainsChoice(List<UUID> playerIds, int playerIndex, List<Integer> tappedCounts,
                                    List<UUID> chosenPlains, String sourceName)
            implements MultiPermanentChoiceContext {

        public RaidingPartyPlainsChoice {
            playerIds = List.copyOf(playerIds);
            tappedCounts = List.copyOf(tappedCounts);
            chosenPlains = List.copyOf(chosenPlains);
        }
    }

    /** The controller chooses exactly two creatures; their power difference determines the effect. */
    record ChooseTwoCreaturesByPowerDifference() implements MultiPermanentChoiceContext {
    }

    /**
     * Tap each chosen untapped creature the controller controls, then the controller creates one
     * {@code tokenTemplate} token per creature tapped this way (Devout Invocation). The template's
     * own amount is ignored; {@code sourceSetCode} is the set code of the spell that created the
     * tokens, used for the token's art.
     */
    record TapCreaturesCreateTokens(com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate,
                                    String sourceSetCode) implements MultiPermanentChoiceContext {
    }

    /** Tap any number of creatures, then queue the target-dependent reflexive ability. */
    record TapCreaturesThenQueueReflexiveAbility(StackEntry resolvingEntry,
                                                 CardEffect reflexiveEffect)
            implements MultiPermanentChoiceContext {
    }

    /** Tap the chosen permanents, then draw a card for each permanent tapped (Guild Summit). */
    record TapPermanentsDrawPerTapped() implements MultiPermanentChoiceContext {
    }

    /** Tap each chosen permanent and put one counter of the specified type on it. */
    record TapPermanentsAndPutCounters(CounterType counterType) implements MultiPermanentChoiceContext {
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
     * Sacrifice the chosen permanents, then add one mana of {@code color} to the controller's pool
     * for each one actually sacrificed (Mana Seism).
     */
    record SacrificePermanentsAddManaPerSacrificed(ManaColor color) implements MultiPermanentChoiceContext {
    }

    /** The controller chooses none or exactly {@code requiredCount} permanents to sacrifice. */
    record SacrificePermanentsOrElse(int requiredCount, CardEffect sacrificedEffect,
                                     CardEffect elseEffect) implements MultiPermanentChoiceContext {
    }

    /**
     * Sacrifice the chosen permanents and record their count, and optionally their total power, on
     * the resolving stack entry for a following effect.
     */
    record SacrificeAnyNumberAndRecordCount(StackEntry resolvingEntry,
                                            boolean recordSacrificedPower)
            implements MultiPermanentChoiceContext {

        public SacrificeAnyNumberAndRecordCount(StackEntry resolvingEntry) {
            this(resolvingEntry, false);
        }
    }

    /** The controller chose distinct artifact and/or creature tokens to copy. */
    record CreateTokenCopiesOfChosenDistinctControlledTokens() implements MultiPermanentChoiceContext {
    }

    /** Exile any number of selected permanents until the source permanent leaves the battlefield. */
    record ExileAnyNumberUntilSourceLeaves(UUID sourcePermanentId) implements MultiPermanentChoiceContext {
    }

    /**
     * "Sacrifice [source] unless you sacrifice any number of creatures with total power
     * {@code requiredPower} or greater" (Phyrexian Dreadnought). The chosen creatures are
     * sacrificed only when their total effective power reaches {@code requiredPower}; an empty
     * selection sacrifices {@code sourcePermanentId} instead.
     */
    record SacrificeCreaturesWithTotalPowerOrSacrificeSource(UUID sourcePermanentId, int requiredPower)
            implements MultiPermanentChoiceContext {
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
     * Dracoplasm: the entering creature's controller chose which of their other creatures to
     * sacrifice as it enters (CR 614.1c). The chosen creatures are sacrificed, the entering
     * permanent's base power and toughness are set to their total power and total toughness, then
     * the creature's ETB triggers proceed. Carries the entry context needed to resume
     * {@code processCreatureETBEffects}.
     */
    record SacrificeCreaturesSetEnteringPowerToughness(UUID enteringPermanentId, UUID controllerId, Card card,
                                                       UUID targetId, boolean wasCastFromHand, int etbMode,
                                                       boolean kicked)
            implements MultiPermanentChoiceContext {
    }

    /**
     * "As this permanent enters, sacrifice any number of permanents. It enters with that many +1/+1
     * counters on it" (Shimatsu the Bloodcloaked): the entering permanent's controller chose which of
     * their permanents to sacrifice as it enters. The chosen permanents are sacrificed, the entering
     * permanent receives {@code countersPerPermanent} times that many +1/+1 counters, then its ETB
     * triggers proceed. Carries the entry context needed to resume
     * {@code processCreatureETBEffects}.
     */
    record SacrificeAsEntersForCounters(UUID enteringPermanentId, int countersPerPermanent,
                                        UUID controllerId, Card card, UUID targetId,
                                        boolean wasCastFromHand, int etbMode, boolean kicked)
            implements MultiPermanentChoiceContext {
    }

    /** A permanent entering by sacrificing an exact number of matching permanents, or declining. */
    record SacrificePermanentsToEnter(UUID controllerId, Permanent enteringPermanent, int requiredCount)
            implements MultiPermanentChoiceContext {
    }

    /**
     * Magnetic Mountain / Thelon's Curse: the acting player ({@code actingPlayerId}, the player
     * whose upkeep it is) chose any number of their tapped matching creatures (up to what they can
     * afford). They pay {@code manaCost} for each chosen creature from their mana pool, then those
     * creatures untap. The choice was already capped at begin time by the mana available, so
     * payment always succeeds; the empty selection means "untap none".
     */
    record PayManaPerCreatureUntap(UUID actingPlayerId, String manaCost)
            implements MultiPermanentChoiceContext {

        public PayManaPerCreatureUntap(UUID actingPlayerId, int manaPerCreature) {
            this(actingPlayerId, "{" + manaPerCreature + "}");
        }
    }

    /**
     * Tetravus second upkeep trigger: exile the chosen tokens (each of which must be a token
     * {@code sourcePermanentId} created, tracked in {@code GameData.sourceCreatedTokens}), then put
     * that many +1/+1 counters on the source.
     */
    record ExileTetraviteTokensPutCountersOnSource(UUID sourcePermanentId)
            implements MultiPermanentChoiceContext {
    }

    /** The controller chose other nontoken creatures to exile until the source leaves. */
    record ExileOwnNontokenCreaturesUntilSourceLeaves(UUID sourcePermanentId)
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

    /**
     * Killing Wave: the choosing player selected creatures they control to keep by paying
     * {@code xValue} life each (capped by affordable life at begin time). Choices accumulate across
     * APNAP players in {@code accumulatedKeepIds}; {@code remainingPlayerIds} still need to choose.
     * After the last chooser, all life is paid and non-kept creatures are sacrificed simultaneously.
     */
    record KillingWaveKeep(UUID choosingPlayerId, int xValue, String sourceName,
                           java.util.List<UUID> remainingPlayerIds,
                           java.util.List<UUID> accumulatedKeepIds)
            implements MultiPermanentChoiceContext {
    }

    /** Each player chose creatures to keep whose total power is at most a fixed limit. */
    record EachPlayerChoosesCreaturesWithTotalPowerAtMostChoice(
            UUID choosingPlayerId, int maxPower, java.util.List<UUID> remainingPlayerIds,
            java.util.List<UUID> accumulatedKeepIds, String sourceName)
            implements MultiPermanentChoiceContext {
    }

    /** Fade Away: the player selected creatures whose controllers will pay instead of sacrificing. */
    record FadeAwayKeep(UUID choosingPlayerId, java.util.List<UUID> creatureIds,
                        java.util.List<UUID> remainingPlayerIds,
                        java.util.List<UUID> accumulatedKeepIds,
                        java.util.List<UUID> accumulatedSacrificeIds,
                        UUID sourceControllerId, String sourceName, String manaCost)
            implements MultiPermanentChoiceContext {
    }

    /** Fade Away: the player selected the permanents sacrificed for the creatures not paid for. */
    record FadeAwaySacrifice(UUID choosingPlayerId, java.util.List<UUID> creatureIds,
                             int requiredCount, java.util.List<UUID> remainingPlayerIds,
                             java.util.List<UUID> accumulatedKeepIds,
                             java.util.List<UUID> accumulatedSacrificeIds,
                             UUID sourceControllerId, String sourceName, String manaCost)
            implements MultiPermanentChoiceContext {
    }

    /** Carries a keep-one-per-type choice until the next type pass or final sacrifice. */
    record KeepOneOfEachTypeChoice(UUID controllerId, UUID subjectPlayerId, CardType typePhase,
                                   java.util.List<UUID> affectedPlayerIds,
                                   java.util.List<UUID> remainingPlayerIds,
                                   java.util.List<UUID> keptIds, String sourceName,
                                   java.util.List<CardType> types, boolean sacrificeAllPermanents,
                                   boolean eachPlayerChooses)
            implements MultiPermanentChoiceContext {
    }

    /**
     * Winnowing: the spell's controller chose a creature for the player at {@code playerIndex}.
     * The choices are retained by player so every player's other non-sharing creatures can be
     * sacrificed together after all choices are complete.
     */
    record WinnowingChoice(UUID controllerId, List<UUID> playerIds, int playerIndex,
                           Map<UUID, UUID> chosenByPlayer, String sourceName)
            implements MultiPermanentChoiceContext {

        public WinnowingChoice {
            playerIds = List.copyOf(playerIds);
            chosenByPlayer = Map.copyOf(chosenByPlayer);
        }
    }

    /** Krenko's Buzzcrusher: the controller chooses up to one nonbasic land for each player. */
    record DestroyUpToOneNonbasicLandPerPlayerChoice(UUID controllerId, List<UUID> playerIds,
                                                     int playerIndex, List<UUID> selectedIds,
                                                     String sourceName)
            implements MultiPermanentChoiceContext {

        public DestroyUpToOneNonbasicLandPerPlayerChoice {
            playerIds = List.copyOf(playerIds);
            selectedIds = List.copyOf(selectedIds);
        }
    }

    /**
     * Release-style choice: the current player chooses one permanent for the current card type.
     * The current player's picks and the already completed players' picks are carried separately so
     * a multi-typed permanent can be chosen for more than one listed type.
     */
      record EachPlayerSacrificeOneOfEachTypeChoice(java.util.List<UUID> playerIds, int playerIndex,
                                                    int typeIndex, java.util.List<UUID> accumulatedIds,
                                                    java.util.List<UUID> currentPlayerIds,
                                                    String sourceName)
            implements MultiPermanentChoiceContext {
        public EachPlayerSacrificeOneOfEachTypeChoice {
            playerIds = java.util.List.copyOf(playerIds);
            accumulatedIds = java.util.List.copyOf(accumulatedIds);
            currentPlayerIds = java.util.List.copyOf(currentPlayerIds);
          }
      }

      /** Deadly Brew: each player chooses a creature or planeswalker before all are sacrificed. */
      record EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnChoice(
              UUID effectControllerId, java.util.List<UUID> playerIds, int playerIndex,
              java.util.Map<UUID, UUID> chosenByPlayer,
              String sourceName) implements MultiPermanentChoiceContext {
          public EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnChoice {
              playerIds = java.util.List.copyOf(playerIds);
              chosenByPlayer = java.util.Map.copyOf(chosenByPlayer);
          }
      }

    /**
     * Global Ruin: the current player chose a land for the current basic land type. The kept
     * permanent IDs accumulate across all players and type passes until the remaining lands are
     * sacrificed.
     */
    record EachPlayerChoosesLandOfEachBasicTypeChoice(java.util.List<UUID> playerIds, int playerIndex,
                                                      int typeIndex, java.util.List<UUID> keptIds,
                                                      String sourceName)
            implements MultiPermanentChoiceContext {
        public EachPlayerChoosesLandOfEachBasicTypeChoice {
            playerIds = java.util.List.copyOf(playerIds);
            keptIds = java.util.List.copyOf(keptIds);
        }
    }

    /** Planar Overlay: the current player chose a land for the current basic land type. */
    record EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandChoice(
            java.util.List<UUID> playerIds, int playerIndex, int typeIndex,
            java.util.List<UUID> selectedIds, String sourceName)
            implements MultiPermanentChoiceContext {
        public EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandChoice {
            playerIds = java.util.List.copyOf(playerIds);
            selectedIds = java.util.List.copyOf(selectedIds);
        }
    }

    /** Sundering Titan: the controller chose a land for the current basic land type. */
    record ChooseLandOfEachBasicTypeThenDestroyChoice(UUID controllerId, int typeIndex,
                                                      java.util.List<UUID> chosenIds,
                                                      String sourceName)
            implements MultiPermanentChoiceContext {
        public ChooseLandOfEachBasicTypeThenDestroyChoice {
            chosenIds = java.util.List.copyOf(chosenIds);
        }
    }

    /**
     * Urza's Sylex-style choice: the current player chooses the required number of lands to keep
     * before the effect destroys every other permanent.
     */
    record EachPlayerChoosesLandsThenDestroyRestChoice(java.util.List<UUID> playerIds, int playerIndex,
                                                       int requiredCount, java.util.List<UUID> keptIds,
                                                       String sourceName)
            implements MultiPermanentChoiceContext {
        public EachPlayerChoosesLandsThenDestroyRestChoice {
            playerIds = java.util.List.copyOf(playerIds);
            keptIds = java.util.List.copyOf(keptIds);
        }
    }

    /**
     * Equipoise: the controller chose permanents of {@code phase} that {@code targetPlayerId}
     * controls to phase out (one pass of land / artifact / creature). Completion phases them out
     * then advances to the next pass via {@code EquipoiseSupport}.
     */
    record EquipoisePhaseOut(Card sourceCard, UUID controllerId, UUID targetPlayerId, EquipoisePhase phase)
            implements MultiPermanentChoiceContext {
    }
}
