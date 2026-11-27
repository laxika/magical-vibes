package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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

    /** Selects an optional group of player targets without reserving the controller as a decline sentinel. */
    record EtbPlayerTargetGroup(PermanentChoiceContext.ETBTokenMultiTargetTrigger pending)
            implements MultiPermanentChoiceContext {}

    /** Selects the two players remembered by a permanent as it enters the battlefield. */
    record ChoosePlayersAsEnter(PermanentChoiceContext.ChoosePlayerAsEnter pending)
            implements MultiPermanentChoiceContext {}

    /** Selects an optional graveyard-card target while walking an ETB or planar target group. */
    record EtbGraveyardCardTargetGroup(PermanentChoiceContext.ETBTokenMultiTargetTrigger pending)
            implements MultiPermanentChoiceContext {}

    record RemoveCounterFromTwoCreatures(StackEntry resolvingEntry, CounterType counterType,
                                         CardEffect thenEffect)
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

    /** The activating player chooses any number of permanents to sacrifice as an ability cost. */
    record ActivatedAbilitySacrificeAnyNumberCost(UUID playerId, UUID sourcePermanentId, int abilityIndex,
                                                   int xValue, UUID targetId, Zone targetZone,
                                                   List<UUID> targetIds, Map<UUID, Integer> damageAssignments,
                                                   ActivatedAbility ability, Permanent sourcePermanentSnapshot,
                                                   SacrificeAnyNumberOfPermanentsCost cost)
            implements MultiPermanentChoiceContext {

        public ActivatedAbilitySacrificeAnyNumberCost {
            targetIds = targetIds == null ? List.of() : List.copyOf(targetIds);
            damageAssignments = damageAssignments == null ? Map.of() : Map.copyOf(damageAssignments);
        }
    }

    /** A Saga chapter's targets for its counters; {@code triggerRecord} is the entry the stack door parked. */
    record SagaChapterCounterDistribution(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, String chapterName, CounterType counterType,
                                          int total, StackEntry triggerRecord) implements MultiPermanentChoiceContext {

        public SagaChapterCounterDistribution {
            effects = List.copyOf(effects);
        }
    }

    /**
     * The controller selected any number of player targets for a Saga chapter; {@code triggerRecord} is the entry the
     * stack door parked.
     */
    record SagaChapterTargetSelection(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                      UUID sourcePermanentId, String chapterName, int minTargets,
                                      StackEntry triggerRecord)
            implements MultiPermanentChoiceContext {

        public SagaChapterTargetSelection {
            effects = List.copyOf(effects);
        }
    }

    /** Selects multiple permanent targets for an attack trigger. */
    record AttackTriggerTargets(PermanentChoiceContext.AttackTriggerTarget pending, int minTargets)
            implements MultiPermanentChoiceContext {
    }

    /** One pile selection in Camouflage's replacement for normal blocker declaration. */
    record CamouflagePileChoice(UUID defenderId, List<Integer> attackerIndices,
                                List<UUID> creatureIds, List<List<UUID>> piles,
                                Card sourceCard) implements MultiPermanentChoiceContext {

        public CamouflagePileChoice {
            attackerIndices = List.copyOf(attackerIndices);
            creatureIds = List.copyOf(creatureIds);
            piles = piles.stream().map(List::copyOf).toList();
        }
    }

    /**
     * Chooses the one to {@code maxTargets} targets of a divided-damage or divided-prevention
     * triggered ability already on the stack, before any player receives priority (CR 603.3d); the
     * division follows. {@code prevention} marks a division of damage prevention, a non-null
     * {@code counterType} a distribution of counters of that type, and {@code allowsNoTargets} an "any number of
     * targets" that may be no target at all (Kuldotha Flamefiend).
     */
    record StackedDividedDamageTargets(UUID stackEntryId, Card sourceCard, UUID controllerId, int total,
                                       boolean prevention, CounterType counterType, boolean allowsNoTargets)
            implements MultiPermanentChoiceContext {
    }

    record CounterDistribution(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                UUID sourcePermanentId, CounterType counterType, int total,
                                PermanentChoiceContext.HandCastSpellTarget spellCast)
            implements MultiPermanentChoiceContext {

        public CounterDistribution(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                   UUID sourcePermanentId, CounterType counterType, int total) {
            this(sourceCard, controllerId, effects, sourcePermanentId, counterType, total, null);
        }

        public CounterDistribution {
            effects = List.copyOf(effects);
        }
    }

    /** Deal damage to a creature the damaged player controls (combat damage trigger). */
    record DealDamageToDamagedPlayerControls(StackEntry damageEntry, DynamicAmount damage)
            implements MultiPermanentChoiceContext {

        public DealDamageToDamagedPlayerControls(StackEntry damageEntry, int damage) {
            this(damageEntry, new com.github.laxika.magicalvibes.model.amount.Fixed(damage));
        }
    }

    /**
     * Untap the single chosen permanent (any battlefield), for a triggered ability with no cast-time
     * target — e.g. Initiate's Companion's "untap target creature or land". The choosable permanents
     * were narrowed at begin time by the effect's predicate.
     */
    record UntapChosenPermanent(String sourceName) implements MultiPermanentChoiceContext {
    }

    /**
     * "You may sacrifice [source]. If you do, destroy target creature that player controls."
     * {@code sourceSacrificed} is set for the completion's second departure, the destroy, once the source has been
     * sacrificed, so completing it again after a CR 616.1 question doesn't sacrifice again.
     */
    record SacrificeSelfToDestroy(UUID sourcePermanentId, boolean cannotBeRegenerated, boolean sourceSacrificed)
            implements MultiPermanentChoiceContext {

        public SacrificeSelfToDestroy(UUID sourcePermanentId, boolean cannotBeRegenerated) {
            this(sourcePermanentId, cannotBeRegenerated, false);
        }
    }

    /** The controller selected Equipment they control to attach to the targeted creature. */
    record AttachAnyNumberOfControlledEquipmentToTargetCreature(UUID targetCreatureId)
            implements MultiPermanentChoiceContext {
    }

    /** Inventory Management: select the controlled Auras and Equipment to reattach individually. */
    record InventoryManagementAttachmentSelection(UUID controllerId)
            implements MultiPermanentChoiceContext {
    }

    /** Selects any number of captured Auras and Equipment to move onto a death-trigger target. */
    record ReturnAurasAndAttachEquipmentToTargetCreature(UUID targetCreatureId,
                                                         List<UUID> auraCardIds,
                                                         List<UUID> equipmentPermanentIds)
            implements MultiPermanentChoiceContext {

        public ReturnAurasAndAttachEquipmentToTargetCreature {
            auraCardIds = List.copyOf(auraCardIds);
            equipmentPermanentIds = List.copyOf(equipmentPermanentIds);
        }
    }

    /** The controller may choose an Equipment attached to a creature they control to unattach. */
    record UnattachEquipmentFromControlledCreature(StackEntry resolvingEntry)
            implements MultiPermanentChoiceContext {
    }

    /** The attacking player chooses permanents to sacrifice as an additional attack cost. */
    record SacrificeAttackCost(int requiredCount) implements MultiPermanentChoiceContext {
    }

    /** The attacking player chooses permanents to return to their owners' hands as an additional attack cost. */
    record ReturnToHandAttackCost(int requiredCount) implements MultiPermanentChoiceContext {
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
     * The spell's controller chooses the targets of the mode an opponent chose for the spell (Fatal Lore: "you
     * choose the zero, one, or two targets after that", its ruling; CR 601.2c). The chosen permanents become the
     * targets of the spell on the stack ({@code stackEntryId}), each recorded as controlled by
     * {@code targetControllerId} ("target creatures that player controls"), with {@code targetFilter}, the mode's,
     * checked again as the spell resolves.
     */
    record TargetsAfterOpponentModeChoice(UUID stackEntryId, UUID targetControllerId,
                                          com.github.laxika.magicalvibes.model.filter.TargetFilter targetFilter)
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
     * The caster chooses which untapped creatures to tap for convoke while casting {@code card} from exile
     * for its madness cost {@code manaCost} (plus {@code lifeCost} life). Choosing none declines the cast.
     */
    record MadnessConvoke(Card card, String manaCost, int lifeCost) implements MultiPermanentChoiceContext {
    }

    /**
     * The caster chooses which permanents to tap for a "tap N untapped permanents" additional cost
     * of a spell cast without paying its mana cost, or to tap or sacrifice for its escalate cost;
     * {@code entry} is put on the stack once the cost is paid.
     */
    record FreeCastTapCost(StackEntry entry, int requiredCount) implements MultiPermanentChoiceContext {
    }

    /**
     * The controller untaps the chosen permanents (Rewind's "Untap up to four lands").
     * {@code sourceName} names the source in the game log.
     */
    record UntapChosenPermanents(String sourceName) implements MultiPermanentChoiceContext {
    }

    /**
     * The payer of a cost a resolving effect asked for (an "unless" ransom, CR 118.12a) chooses the permanents that pay
     * it: exactly {@code minimum} to sacrifice, or between {@code minimum} and the cost's amount to tap for a
     * waterbend (CR 701.67a), the rest paid with mana. The asking effect then resumes with the choice to pay made.
     */
    record ResolutionCostPayment(com.github.laxika.magicalvibes.model.effect.CostEffect cost, UUID payerId,
                                 Card sourceCard, UUID sourceControllerId, int minimum)
            implements MultiPermanentChoiceContext {
    }

    /**
     * The affected player chooses the required number of permanents to untap, or fewer when
     * {@code upTo} is true.
     */
    record UntapPermanentsForAmount(String sourceName, int requiredCount, boolean upTo)
            implements MultiPermanentChoiceContext {

        public UntapPermanentsForAmount(String sourceName, int requiredCount) {
            this(sourceName, requiredCount, false);
        }
    }

    /** The controller returns the chosen permanents to their owners' hands (Resounding Wave cycling trigger). */
    record ReturnTargetPermanentsToHand() implements MultiPermanentChoiceContext {
    }

    record PlayerChoosesUpToPermanentsThenSacrificesRestChoice(List<UUID> candidateIds)
            implements MultiPermanentChoiceContext {
        public PlayerChoosesUpToPermanentsThenSacrificesRestChoice {
            candidateIds = List.copyOf(candidateIds);
        }
    }

    /**
     * "Each player chooses N permanents to keep, then sacrifices the rest" (CR 101.4): {@code current} is the
     * player being prompted, whose {@link PendingForcedSacrifice#count()} is how many permanents to keep.
     * Sacrifices accumulate across prompts and happen simultaneously once every player has chosen.
     */
    record KeepForEachPlayerThenSacrificeRest(PendingForcedSacrifice current,
                                              List<PendingForcedSacrifice> remainingChoosers,
                                              List<UUID> accumulatedSacrificeIds)
            implements MultiPermanentChoiceContext {
        public KeepForEachPlayerThenSacrificeRest {
            remainingChoosers = List.copyOf(remainingChoosers);
            accumulatedSacrificeIds = List.copyOf(accumulatedSacrificeIds);
        }
    }

    /** The controller's choice of a {@code ReturnChosenPermanentsToHandEffect}: at least {@code minimum} of the offered. */
    record ReturnChosenPermanentsToHand(
            com.github.laxika.magicalvibes.model.effect.ReturnChosenPermanentsToHandEffect effect, int minimum)
            implements MultiPermanentChoiceContext {
    }

    /** Phase out up to N matching permanents controlled by the resolving player. */
    record PhaseOutUpToNControlledPermanents(
            com.github.laxika.magicalvibes.model.effect.PhaseOutUpToNControlledPermanentsEffect effect)
            implements MultiPermanentChoiceContext {
    }

    /** Resolve one choice in a repeated immediate controller-creature flicker. */
    record FlickerAnyNumber(StackEntry resolvingEntry,
                            com.github.laxika.magicalvibes.model.effect.FlickerEffect effect,
                            int remainingIterations) implements MultiPermanentChoiceContext {
    }

    /** Exile the selected face-up creatures with disguise and cloak them. */
    record ExileInFaceDownPile(StackEntry resolvingEntry,
                               com.github.laxika.magicalvibes.model.effect.ExileFaceDownPileEffect effect)
            implements MultiPermanentChoiceContext {
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
     * {@code recordSacrificedPower} carries the greatest effective power sacrificed back to the
     * parked stack entry for a following effect.
     */
    record ForcedSacrifice(UUID sacrificingPlayerId,
                           java.util.List<PendingForcedSacrifice> remainingChoosers,
                           java.util.List<UUID> accumulatedSacrificeIds,
                           boolean simultaneousFlow,
                           boolean recordSacrificedCount,
                           boolean recordSacrificedPower,
                           LibrarySearchFollowUp afterSacrifices)
            implements MultiPermanentChoiceContext {

        public ForcedSacrifice(UUID sacrificingPlayerId,
                               java.util.List<PendingForcedSacrifice> remainingChoosers,
                               java.util.List<UUID> accumulatedSacrificeIds) {
            this(sacrificingPlayerId, remainingChoosers, accumulatedSacrificeIds, false, false, false, null);
        }

        public ForcedSacrifice(UUID sacrificingPlayerId,
                               java.util.List<PendingForcedSacrifice> remainingChoosers,
                               java.util.List<UUID> accumulatedSacrificeIds,
                               boolean simultaneousFlow) {
            this(sacrificingPlayerId, remainingChoosers, accumulatedSacrificeIds,
                    simultaneousFlow, false, false, null);
        }

        public ForcedSacrifice(UUID sacrificingPlayerId,
                               java.util.List<PendingForcedSacrifice> remainingChoosers,
                               java.util.List<UUID> accumulatedSacrificeIds,
                               boolean simultaneousFlow,
                               boolean recordSacrificedCount) {
            this(sacrificingPlayerId, remainingChoosers, accumulatedSacrificeIds,
                    simultaneousFlow, recordSacrificedCount, false, null);
        }

        public ForcedSacrifice(UUID sacrificingPlayerId,
                               java.util.List<PendingForcedSacrifice> remainingChoosers,
                               java.util.List<UUID> accumulatedSacrificeIds,
                               boolean simultaneousFlow,
                               boolean recordSacrificedCount,
                               LibrarySearchFollowUp afterSacrifices) {
            this(sacrificingPlayerId, remainingChoosers, accumulatedSacrificeIds,
                    simultaneousFlow, recordSacrificedCount, false, afterSacrifices);
        }
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

    /** Each player chooses a creature; all chosen creatures are then sacrificed together. */
    record EachPlayerSacrificesCreatureCreateTokenEqualToTotalPower(
            java.util.List<PendingForcedSacrifice> remainingChoosers,
            java.util.List<UUID> accumulatedSacrificeIds,
            CreateTokenEffect tokenTemplate,
            StackEntry resolvingEntry)
            implements MultiPermanentChoiceContext {
        public EachPlayerSacrificesCreatureCreateTokenEqualToTotalPower {
            remainingChoosers = java.util.List.copyOf(remainingChoosers);
            accumulatedSacrificeIds = java.util.List.copyOf(accumulatedSacrificeIds);
        }
    }

    /** Each opponent chooses a creature before the chosen creatures are sacrificed together. */
    record EachOpponentSacrificesCreatureCreateTokens(
            java.util.List<PendingForcedSacrifice> remainingChoosers,
            java.util.List<UUID> accumulatedSacrificeIds,
            CreateTokenEffect tokenTemplate,
            StackEntry resolvingEntry)
            implements MultiPermanentChoiceContext {
        public EachOpponentSacrificesCreatureCreateTokens {
            remainingChoosers = java.util.List.copyOf(remainingChoosers);
            accumulatedSacrificeIds = java.util.List.copyOf(accumulatedSacrificeIds);
        }
    }

    /** Each opponent chooses a matching permanent before the chosen permanents are sacrificed together. */
    record EachOpponentSacrificesPermanentCreateTokens(
            java.util.List<PendingForcedSacrifice> remainingChoosers,
            java.util.List<UUID> accumulatedSacrificeIds,
            com.github.laxika.magicalvibes.model.filter.PermanentPredicate sacrificeFilter,
            CreateTokenEffect tokenTemplate,
            StackEntry resolvingEntry)
            implements MultiPermanentChoiceContext {
        public EachOpponentSacrificesPermanentCreateTokens {
            remainingChoosers = java.util.List.copyOf(remainingChoosers);
            accumulatedSacrificeIds = java.util.List.copyOf(accumulatedSacrificeIds);
        }
    }

    /** Each opponent chooses a nontoken creature to sacrifice for Replicating Terror. */
    record EachOpponentSacrificesNontokenCreatureConjuresDuplicates(
            java.util.List<PendingForcedSacrifice> remainingChoosers,
            java.util.List<UUID> accumulatedSacrificeIds,
            StackEntry resolvingEntry)
            implements MultiPermanentChoiceContext {
        public EachOpponentSacrificesNontokenCreatureConjuresDuplicates {
            remainingChoosers = java.util.List.copyOf(remainingChoosers);
            accumulatedSacrificeIds = java.util.List.copyOf(accumulatedSacrificeIds);
        }
    }

    /** The controller and a target opponent each choose a creature before both are sacrificed. */
    record ControllerAndTargetPlayerChooseCreaturesThenSacrifice(
            java.util.List<PendingForcedSacrifice> remainingChoosers,
            java.util.List<UUID> accumulatedSacrificeIds,
            StackEntry resolvingEntry)
            implements MultiPermanentChoiceContext {
        public ControllerAndTargetPlayerChooseCreaturesThenSacrifice {
            remainingChoosers = java.util.List.copyOf(remainingChoosers);
            accumulatedSacrificeIds = java.util.List.copyOf(accumulatedSacrificeIds);
        }
    }

    /** Speedbrood Stalker: privately choose the target player's creature or planeswalker, then resolve the sacrifices. */
    record TargetPlayerChoosesCreatureOrPlaneswalkerThenSacrificesChosen(
            UUID targetPlayerId,
            UUID sourceControllerId,
            UUID chosenPermanentId,
            String sourceCardName)
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
                             String choicePrompt, boolean requiresChoice, int requiredCount)
            implements MultiPermanentChoiceContext {
    }

    /**
     * A player who chose to pay a "[any player] may sacrifice / tap N [permanents]" ransom paid together
     * ({@code UnlessPaysEffect.payingTogether()}, Prowling Pangolin) picks which ones; the picks wait on the resolving
     * entry's {@code UnlessAnswer} until every player has answered.
     */
    record AnyPlayerPaymentPick(UUID playerId) implements MultiPermanentChoiceContext {
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

    /** Each player chooses one permanent to return to its owner's hand in active-player order. */
    record EachPlayerReturnsPermanent(List<UUID> remainingPlayerIds, List<UUID> chosenIds,
                                      String sourceName) implements MultiPermanentChoiceContext {

        public EachPlayerReturnsPermanent {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            chosenIds = List.copyOf(chosenIds);
        }
    }

    /** Each player chooses up to one qualifying permanent controlled by an opponent. */
    record EachPlayerChoosesOpponentPermanentToExile(List<UUID> remainingPlayerIds,
                                                     List<UUID> chosenIds,
                                                     PermanentPredicate filter,
                                                     UUID sourceCardId,
                                                     UUID sourceControllerId,
                                                     String sourceName) implements MultiPermanentChoiceContext {

        public EachPlayerChoosesOpponentPermanentToExile {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            chosenIds = List.copyOf(chosenIds);
        }
    }

    /** Each player chooses up to one (or, when {@code mandatory}, exactly one) qualifying permanent controlled by an opponent. */
    record EachPlayerChoosesOpponentPermanentToDestroy(List<UUID> remainingPlayerIds,
                                                       List<UUID> chosenIds,
                                                       PermanentPredicate filter,
                                                       UUID sourceCardId,
                                                       UUID sourceControllerId,
                                                       String sourceName,
                                                       boolean opponentsOfSourceController,
                                                       boolean mandatory) implements MultiPermanentChoiceContext {

        public EachPlayerChoosesOpponentPermanentToDestroy {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            chosenIds = List.copyOf(chosenIds);
        }
    }

    /** Each player chooses a different qualifying permanent controlled by an opponent to keep. */
    record EachPlayerChoosesDifferentOpponentPermanentThenDestroyRest(
            List<UUID> remainingPlayerIds,
            List<UUID> chosenIds,
            PermanentPredicate filter,
            UUID sourceCardId,
            UUID sourceControllerId,
            String sourceName) implements MultiPermanentChoiceContext {

        public EachPlayerChoosesDifferentOpponentPermanentThenDestroyRest {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            chosenIds = List.copyOf(chosenIds);
        }
    }

    /** Put {@code count} counters of {@code counterType} on the single chosen own permanent. */
    record OwnPermanentCounterPlacement(CounterType counterType, int count, boolean mandatory)
            implements MultiPermanentChoiceContext {
        public OwnPermanentCounterPlacement(CounterType counterType, int count) {
            this(counterType, count, false);
        }
    }

    /** Put one or more counters on each chosen matching permanent the controller controls. */
    record OwnPermanentCounterPlacementOnChosenPermanents(
            CounterType counterType, int count, PermanentPredicate permanentFilter)
            implements MultiPermanentChoiceContext {
    }

    /** Put counters on the single chosen permanent on any battlefield. */
    record AnyPermanentCounterPlacement(CounterType counterType, int count)
            implements MultiPermanentChoiceContext {
    }

    /**
     * Put counters on the chosen permanent and attribute the placement to the choosing player.
     * When {@code mandatory}, the chooser must pick exactly one permanent.
     */
    record OwnPermanentCounterPlacementByPlayer(CounterType counterType, int count, UUID placingPlayerId,
                                                boolean mandatory)
            implements MultiPermanentChoiceContext {

        public OwnPermanentCounterPlacementByPlayer(CounterType counterType, int count, UUID placingPlayerId) {
            this(counterType, count, placingPlayerId, false);
        }
    }

    /** Put counters on the chosen permanent, attribute them to another player, and remember it. */
    record OwnPermanentCounterPlacementByPlayerWithChosenReference(
            CounterType counterType, int count, UUID placingPlayerId, boolean mandatory)
            implements MultiPermanentChoiceContext {

        public OwnPermanentCounterPlacementByPlayerWithChosenReference(
                CounterType counterType, int count, UUID placingPlayerId) {
            this(counterType, count, placingPlayerId, false);
        }
    }

    /** Put one counter on an opponent's creature for each remaining cumulative-upkeep payment. */
    record OpponentCreatureCounterPlacement(CounterType counterType, int remainingCount,
                                             UUID placingPlayerId) implements MultiPermanentChoiceContext {
    }

    /** Put counters on the chosen permanent and remember it for a following effect. */
    record OwnPermanentCounterPlacementWithChosenReference(CounterType counterType, int count, boolean mandatory)
            implements MultiPermanentChoiceContext {

        public OwnPermanentCounterPlacementWithChosenReference(CounterType counterType, int count) {
            this(counterType, count, false);
        }
    }

    /**
     * Proliferate. {@code remainingCount} includes the upcoming pick; each answered pick
     * re-begins with the decremented count (fresh-record-per-pick pattern) until it hits zero.
     */
    record Proliferate(int remainingCount) implements MultiPermanentChoiceContext {
    }

    /**
     * Tap the chosen subtype permanents to boost [source] and damage the player or planeswalker it
     * is attacking (Myr Battlesphere). {@code damageEntry} is the resolving attack trigger: the
     * damage source and the attacked player or planeswalker.
     */
    record TapSubtypeBoost(UUID sourcePermanentId, StackEntry damageEntry) implements MultiPermanentChoiceContext {
    }

    /** Tap the chosen permanents to boost the source by the number tapped (Siege Striker). */
    record TapAnyNumberBoostSelf(UUID sourcePermanentId, int powerPerPermanent, int toughnessPerPermanent)
            implements MultiPermanentChoiceContext {
    }

    /** The controller chooses any number of untapped permanents to tap. */
    record TapAnyNumberPermanents() implements MultiPermanentChoiceContext {
    }

    /**
     * Oracle en-Vec: {@code targetPlayerId} chose any number of creatures they control. The chosen
     * set is made a floating {@code MatchingCreaturesMustAttackEffect} and
     * {@code OnlyMatchingCreaturesCanAttackThisCombatEffect} that apply during that player's next turn — the chosen creatures attack if able, every other creature can't
     * attack, and each chosen creature that didn't attack is destroyed at that turn's end step.
     */
    record ChooseCreaturesToAttackNextTurn(UUID targetPlayerId, UUID controllerId, Card sourceCard)
            implements MultiPermanentChoiceContext {
    }

    /** The controller's choice of a {@code ChoosePermanentsEffect}: at least {@code minimum} of the offered. */
    record ChoosePermanents(com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect effect, int minimum)
            implements MultiPermanentChoiceContext {
    }

    /** The controller chooses equal numbers of creatures from two players for Cultural Exchange. */
    record CulturalExchange(Card sourceCard, UUID chooserId, UUID firstPlayerId, UUID secondPlayerId,
                            List<UUID> firstChosenIds, boolean firstSelection) implements MultiPermanentChoiceContext {

        public CulturalExchange {
            firstChosenIds = List.copyOf(firstChosenIds);
        }

    }

    /** Enlist support selection during attacker declaration, before attack triggers are stacked. */
    record Enlistment(UUID playerId, List<Integer> attackerIndices, Map<Integer, UUID> resolvedTargets,
                      List<Permanent> declaredAttackers, List<UUID> remainingAttackerIds,
                      Set<UUID> usedSupporterIds, Map<UUID, Integer> boostPowers,
                      Map<UUID, UUID> enlistedSupporters)
            implements MultiPermanentChoiceContext {

        public Enlistment {
            attackerIndices = List.copyOf(attackerIndices);
            resolvedTargets = Map.copyOf(resolvedTargets);
            declaredAttackers = List.copyOf(declaredAttackers);
            remainingAttackerIds = List.copyOf(remainingAttackerIds);
            usedSupporterIds = Set.copyOf(usedSupporterIds);
            boostPowers = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(boostPowers));
            enlistedSupporters = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(enlistedSupporters));
        }
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

      /** Nihiloor's optional choice of one untapped creature to tap for one opponent. */
      record NihiloorTapChoice(StackEntry resolvingEntry, UUID opponentId)
              implements MultiPermanentChoiceContext {
      }

    /**
     * Sacrifice the chosen permanents, none included ("sacrifice any number of …"), as the resolving entry's
     * SACRIFICE step: a following "that many" reads {@code AmountThisWay(StepKind.SACRIFICE)}.
     */
    record SacrificeAnyNumberAndRecordCount(StackEntry resolvingEntry) implements MultiPermanentChoiceContext {
    }

    /** The controller chose distinct artifact and/or creature tokens to copy. */
    record CreateTokenCopiesOfChosenDistinctControlledTokens() implements MultiPermanentChoiceContext {
    }

    /** Exile any number of selected permanents until the source permanent leaves the battlefield. */
    record ExileAnyNumberUntilSourceLeaves(UUID sourcePermanentId) implements MultiPermanentChoiceContext {
    }

    /**
     * Lich's Mastery: the controller picked exactly as many permanents they control and cards from
     * their hand or graveyard as the life they lost (or every one of them when there are fewer),
     * and all are exiled.
     */
    record ExileForEachLifeLost() implements MultiPermanentChoiceContext {
    }

    /**
     * Devour (CR 702.82): the entering creature's controller chose which of their other creatures to
     * sacrifice as it enters. The chosen creatures are sacrificed, the entering permanent receives
     * {@code multiplier} times that many +1/+1 counters and records the devoured count, then the
     * creature's ETB triggers proceed. Carries the entry context needed to resume
     * {@code processCreatureETBEffects} for the discard trigger.
     */
    record DevourSacrifice(UUID enteringPermanentId, DynamicAmount multiplier, UUID controllerId, Card card,
                           UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked)
            implements MultiPermanentChoiceContext {
    }

    /**
     * "As this permanent enters, sacrifice any number of [permanents]" ({@code effect}): the entering permanent's
     * controller chose which of their matching permanents to sacrifice as it enters. The chosen permanents are
     * sacrificed together, the entering permanent gets the effect's payoff (counters, total P/T or P/T equal to the
     * count), then its ETB triggers proceed. Carries the entry context needed to resume
     * {@code processCreatureETBEffects}.
     */
    record SacrificeAsEntersForCounters(UUID enteringPermanentId,
                                        com.github.laxika.magicalvibes.model.effect.SacrificePermanentsAsEntersForCountersEffect effect,
                                        UUID controllerId, Card card, UUID targetId,
                                        boolean wasCastFromHand, int etbMode, boolean kicked)
            implements MultiPermanentChoiceContext {
    }

    /** The controller chose any number of matching permanents whose counters are removed as this permanent enters. */
    record RemoveAllCountersAsEntersForCounters(UUID enteringPermanentId,
                                                com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromChosenPermanentsThenEnterWithCountersEffect effect,
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
     * payment always succeeds; the empty selection means "untap none". The mana is a tax paid to
     * {@code taxingPlayerId}, the controller of the ability asking for it, when that is another player.
     */
    record PayManaPerCreatureUntap(UUID actingPlayerId, String manaCost, UUID taxingPlayerId)
            implements MultiPermanentChoiceContext {
    }

    /** Feather, Radiant Arbiter: choose creatures to receive spell copies and pay per creature. */
    record CopySpellForEachOtherCreatureWithMana(
            com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherCreatureWithManaEffect effect)
            implements MultiPermanentChoiceContext {
    }

    /**
     * Tetravus second upkeep trigger: exile the chosen tokens (each of which must be a token
     * {@code sourcePermanentId} created, tracked in {@code GameData.sourceCreatedTokens}), then put
     * that many +1/+1 counters on the source.
     */
    record ExileTetraviteTokensPutCountersOnSource(UUID sourcePermanentId)
            implements MultiPermanentChoiceContext {
    }

    /** Each player chooses the required number of matching permanents they control to exile until the source leaves. */
    record EachPlayerChoosesOwnPermanentsToExileUntilSourceLeaves(
            List<UUID> remainingPlayerIds, List<UUID> chosenIds, PermanentPredicate filter,
            Card sourceCard, UUID sourceControllerId, UUID sourcePermanentId, String sourceName,
            int maxCount, int requiredCount) implements MultiPermanentChoiceContext {

        public EachPlayerChoosesOwnPermanentsToExileUntilSourceLeaves {
            remainingPlayerIds = List.copyOf(remainingPlayerIds);
            chosenIds = List.copyOf(chosenIds);
        }
    }

    /**
     * Static Orb / Stoic Angel: the active player chose up to the cap of the permanents matching
     * {@code filter} that would otherwise untap; only those (plus any permanents the filter excludes)
     * untap this step. A {@code null} filter means all permanents count against the cap (Static Orb).
     * The untap-step bookkeeping and turn advance then resume exactly as they would have without the
     * restriction. When several caps bind at once, {@code remainingRestrictions} lists every binding cap
     * and the single choice must satisfy all of them (Static Orb with Winter Orb or Stoic Angel).
     */
    record StaticOrbUntap(UUID activePlayerId,
                          com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter,
                          List<com.github.laxika.magicalvibes.model.effect.StaticOrbEffect> remainingRestrictions,
                          List<UUID> excludedIds)
            implements MultiPermanentChoiceContext {
        public StaticOrbUntap {
            remainingRestrictions = List.copyOf(remainingRestrictions);
            excludedIds = List.copyOf(excludedIds);
        }
        public StaticOrbUntap(UUID activePlayerId,
                com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter) {
            this(activePlayerId, filter, List.of(), List.of());
        }
    }

    /**
     * Killing Wave: the choosing player selected creatures they control to keep by paying
     * {@code xValue} life each (capped by affordable life at begin time). Choices accumulate across
     * APNAP players in {@code accumulatedKeepIds}; {@code remainingPlayerIds} still need to choose.
     * After the last chooser, all life is paid and non-kept creatures are sacrificed simultaneously.
     */
    record KillingWaveKeep(UUID choosingPlayerId, int xValue, String sourceName,
                           java.util.List<UUID> remainingPlayerIds,
                           java.util.List<UUID> accumulatedKeepIds, UUID sourceControllerId)
            implements MultiPermanentChoiceContext {
    }

    /** Each player chose creatures to keep whose total power is at most a fixed limit. */
    record EachPlayerChoosesCreaturesWithTotalPowerAtMostChoice(
            UUID choosingPlayerId, int maxPower, java.util.List<UUID> remainingPlayerIds,
            java.util.List<UUID> accumulatedKeepIds, String sourceName)
            implements MultiPermanentChoiceContext {
    }

    /** Promise of Loyalty: each player chose the creature that receives a vow counter and survives. */
    record EachPlayerChoosesCreaturePutsVowCounterChoice(
            StackEntry resolvingEntry, java.util.List<UUID> playerIds, int playerIndex,
            java.util.List<UUID> keptIds,
            String sourceName) implements MultiPermanentChoiceContext {

        public EachPlayerChoosesCreaturePutsVowCounterChoice {
            playerIds = List.copyOf(playerIds);
            keptIds = List.copyOf(keptIds);
        }
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

    /** Carries one player's optional party-role choice until the next role or final sacrifice. */
    record EachPlayerChoosesPartyThenSacrificesRestChoice(
            List<UUID> playerIds, int playerIndex, int roleIndex, List<UUID> keptIds,
            String sourceName) implements MultiPermanentChoiceContext {

        public EachPlayerChoosesPartyThenSacrificesRestChoice {
            playerIds = List.copyOf(playerIds);
            keptIds = List.copyOf(keptIds);
        }
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

    /** Consuming Tide: the current player chose the nonland permanent they keep. */
    record EachPlayerChoosesNonlandPermanentThenReturnRestChoice(
            java.util.List<UUID> playerIds, int playerIndex,
            java.util.List<UUID> keptIds, String sourceName)
            implements MultiPermanentChoiceContext {
        public EachPlayerChoosesNonlandPermanentThenReturnRestChoice {
            playerIds = java.util.List.copyOf(playerIds);
            keptIds = java.util.List.copyOf(keptIds);
        }
    }

    /** Disciple of Caelus Nin: the current player chose permanents to keep from phasing out. */
    record EachPlayerChoosesPermanentsThenPhaseOutRestChoice(
            java.util.List<UUID> playerIds, int playerIndex, java.util.List<UUID> chosenIds,
            int maxCount, UUID sourcePermanentId, String sourceName)
            implements MultiPermanentChoiceContext {
        public EachPlayerChoosesPermanentsThenPhaseOutRestChoice {
            playerIds = java.util.List.copyOf(playerIds);
            chosenIds = java.util.List.copyOf(chosenIds);
        }
    }

    /** Each player chooses a nonland permanent they control to receive a counter. */
    record EachPlayerChoosesNonlandPermanentAndPutCounterChoice(
            java.util.List<UUID> playerIds, int playerIndex, java.util.List<UUID> chosenIds,
            CounterType counterType, String sourceName)
            implements MultiPermanentChoiceContext {
        public EachPlayerChoosesNonlandPermanentAndPutCounterChoice {
            playerIds = java.util.List.copyOf(playerIds);
            chosenIds = java.util.List.copyOf(chosenIds);
        }
    }

    /** Each player chose one creature to keep after placing a counter on it. */
    record EachPlayerChoosesCreatureThenSacrificesRestChoice(
            java.util.List<UUID> playerIds, int playerIndex, java.util.List<UUID> chosenIds,
            CounterType counterType, UUID controllerId, String sourceName)
            implements MultiPermanentChoiceContext {
        public EachPlayerChoosesCreatureThenSacrificesRestChoice {
            playerIds = java.util.List.copyOf(playerIds);
            chosenIds = java.util.List.copyOf(chosenIds);
        }
    }

    /** Human—Time Lord Meta-Crisis: each player chooses one or two creatures for an ordered copy. */
    record EachPlayerChoosesOneOrTwoCreaturesCreatesTokenCopyChoice(
            java.util.List<UUID> playerIds, int playerIndex,
            java.util.Map<UUID, java.util.List<UUID>> chosenByPlayer,
            Card sourceCard, UUID controllerId)
            implements MultiPermanentChoiceContext {
        public EachPlayerChoosesOneOrTwoCreaturesCreatesTokenCopyChoice {
            playerIds = java.util.List.copyOf(playerIds);
            java.util.Map<UUID, java.util.List<UUID>> copiedChoices = new java.util.LinkedHashMap<>();
            chosenByPlayer.forEach((playerId, chosenIds) ->
                    copiedChoices.put(playerId, java.util.List.copyOf(chosenIds)));
            chosenByPlayer = java.util.Collections.unmodifiableMap(copiedChoices);
        }
    }

    /**
     * One player casts a vote for an object or a player ({@code VoteEffect} over permanents, cards or players, CR
     * 701.38a, 701.38b). Its queue of voters still to vote lets an answer be replayed with the same voter voting again
     * first ("you may vote an additional time", CR 701.38d).
     *
     * @param effectControllerId the vote's controller, whose "players finish voting" event it is
     * @param remainingVoterIds  the players still to vote after this answer, in voting order
     * @param votes              the votes cast so far
     * @param kindPrefix         the kind of candidate ({@code StepOutcome.Vote.PERMANENT}, {@code CARD} or {@code PLAYER})
     * @param candidateIds       the candidates found as the vote began, those that can't be chosen left out
     * @param otherThanVoter     whether a voter may not vote for themselves ("another player")
     * @param upToOne            whether a voter may cast no vote
     * @param secret             whether the votes are revealed only after the last one
     * @param sourceName         the vote's source as every player reads it
     */
    record ObjectVoteChoice(UUID effectControllerId, List<UUID> remainingVoterIds,
                            List<StepOutcome.Vote> votes, String kindPrefix, List<UUID> candidateIds,
                            boolean otherThanVoter, boolean upToOne, boolean secret, String sourceName)
            implements MultiPermanentChoiceContext, VoteChoiceContext {

        public ObjectVoteChoice {
            remainingVoterIds = List.copyOf(remainingVoterIds);
            votes = List.copyOf(votes);
            candidateIds = List.copyOf(candidateIds);
        }

        /** The candidates {@code voterId} may vote for. */
        public List<UUID> candidatesOf(UUID voterId) {
            return otherThanVoter
                    ? candidateIds.stream().filter(candidateId -> !candidateId.equals(voterId)).toList()
                    : candidateIds;
        }

        /** This context with {@code vote} cast. */
        public ObjectVoteChoice withVote(StepOutcome.Vote vote) {
            List<StepOutcome.Vote> cast = new java.util.ArrayList<>(votes);
            cast.add(vote);
            return new ObjectVoteChoice(effectControllerId, remainingVoterIds, cast, kindPrefix, candidateIds,
                    otherThanVoter, upToOne, secret, sourceName);
        }

        @Override
        public ObjectVoteChoice withRemainingVoterIds(List<UUID> remainingVoterIds) {
            return new ObjectVoteChoice(effectControllerId, remainingVoterIds, votes, kindPrefix, candidateIds,
                    otherThanVoter, upToOne, secret, sourceName);
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

    /** Planetary Annihilation-style choice: the current player chooses lands to keep. */
    record EachPlayerChoosesLandsThenSacrificeRestChoice(java.util.List<UUID> playerIds, int playerIndex,
                                                         int requiredCount, java.util.List<UUID> keptIds,
                                                         String sourceName)
            implements MultiPermanentChoiceContext {
        public EachPlayerChoosesLandsThenSacrificeRestChoice {
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

    /** My Will Is Irresistible: the controller chooses up to three, then the targeted opponent keeps one. */
    record ChooseUpToThreeNonlandPermanentsThenOpponentChoosesOne(
            UUID controllerId, UUID opponentId, List<UUID> chosenPermanentIds,
            String sourceName, boolean opponentChoosing)
            implements MultiPermanentChoiceContext {

        public ChooseUpToThreeNonlandPermanentsThenOpponentChoosesOne {
            chosenPermanentIds = List.copyOf(chosenPermanentIds);
        }
    }
}
