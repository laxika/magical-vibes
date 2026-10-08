package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import org.springframework.beans.factory.ObjectProvider;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.GrantExilePlayPermissionAtNextTurn;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureLibraryReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureReturnToHandReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesDamagedByControlledSourceInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesDamagedBySourceInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesInsteadOfDyingWithLifeLossEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNontokenCreaturesInsteadOfDyingWithBloodCounterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCreaturesOfSubtypeInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentCreatureCardExileReplacement;
import com.github.laxika.magicalvibes.model.effect.PersistReturnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ResolvePendingExileReturnEffect;
import com.github.laxika.magicalvibes.model.effect.PutOnTopOfLibraryInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToBattlefieldUnderOwnerControlEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import com.github.laxika.magicalvibes.model.effect.UndyingReturnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.effect.AuraCopyService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.UnattachTriggerSupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Handles removing permanents from the battlefield and moving them to their destination zones
 * (graveyard, hand, library, or exile). Applies replacement effects (CR 614.6), processes death
 * triggers, handles stolen-creature ownership, and manages related cleanup such as orphaned auras,
 * sacrifice-on-unattach, exile-return-on-leave, and source-linked animations.
 *
 * <p><b>All battlefield removal must go through this service</b> to ensure cross-cutting cleanup
 * is applied consistently. Never call {@code battlefield.remove()} directly from other services.
 */
@Slf4j
@Component
public class PermanentRemovalService {

    private final GraveyardService graveyardService;
    private final BattlefieldEntryService battlefieldEntryService;
    private TriggerCollectionService triggerCollectionService;
    private final DamagePreventionService damagePreventionService;
    private final AuraAttachmentService auraAttachmentService;
    private final GameQueryService gameQueryService;
    private final ObjectProvider<LayerSystemService> layerSystemServiceProvider;
    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final UntapLockReleaseService untapLockReleaseService;
    private final AuraCopyService auraCopyService;
    private final CreatureControlService creatureControlService;
    private final UnattachTriggerSupport unattachTriggerSupport;
    private final LifeSupport lifeSupport;
    private final PlayerInputService playerInputService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final PhasingService phasingService;
    @org.springframework.beans.factory.annotation.Autowired @Lazy
    private com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    public PermanentRemovalService(GraveyardService graveyardService,
                                   BattlefieldEntryService battlefieldEntryService,
                                   @Lazy TriggerCollectionService triggerCollectionService,
                                   DamagePreventionService damagePreventionService,
                                   AuraAttachmentService auraAttachmentService,
                                   GameQueryService gameQueryService,
                                   ObjectProvider<LayerSystemService> layerSystemServiceProvider,
                                   GameLogService gameLogService,
                                   ExileService exileService,
                                   UntapLockReleaseService untapLockReleaseService,
                                   @Lazy AuraCopyService auraCopyService,
                                   @Lazy CreatureControlService creatureControlService,
                                   UnattachTriggerSupport unattachTriggerSupport,
                                   @Lazy LifeSupport lifeSupport,
                                   @Lazy PlayerInputService playerInputService,
                                   EffectHandlerRegistry effectHandlerRegistry,
                                   PhasingService phasingService) {
        this.graveyardService = graveyardService;
        this.battlefieldEntryService = battlefieldEntryService;
        this.triggerCollectionService = triggerCollectionService;
        this.damagePreventionService = damagePreventionService;
        this.auraAttachmentService = auraAttachmentService;
        this.gameQueryService = gameQueryService;
        this.layerSystemServiceProvider = layerSystemServiceProvider;
        this.gameLogService = gameLogService;
        this.exileService = exileService;
        this.untapLockReleaseService = untapLockReleaseService;
        this.auraCopyService = auraCopyService;
        this.creatureControlService = creatureControlService;
        this.unattachTriggerSupport = unattachTriggerSupport;
        this.lifeSupport = lifeSupport;
        this.playerInputService = playerInputService;
        this.effectHandlerRegistry = effectHandlerRegistry;
        this.phasingService = phasingService;
    }

    public void setTriggerCollectionService(TriggerCollectionService triggerCollectionService) {
        this.triggerCollectionService = triggerCollectionService;
    }

    public void beginPermanentLeaveBatch(GameData gameData) {
        if (gameData.permanentLeaveNotificationDepth++ > 0) {
            return;
        }
        gameData.permanentLeaveBatchWatchers.clear();
        gameData.permanentLeaveBatchWatcherControllers.clear();
        gameData.permanentLeaveBatchPendingCreatures.clear();
        gameData.permanentLeaveBatchPendingPermanents.clear();
        gameData.forEachPermanent((controllerId, permanent) -> {
            gameData.permanentLeaveBatchWatchers.put(permanent.getId(), permanent);
            gameData.permanentLeaveBatchWatcherControllers.put(permanent.getId(), controllerId);
        });
    }

    public void endPermanentLeaveBatch(GameData gameData) {
        if (gameData.permanentLeaveNotificationDepth <= 0) {
            return;
        }
        if (--gameData.permanentLeaveNotificationDepth > 0) {
            return;
        }
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldWithoutDyingBatchTriggers(
                gameData, Map.copyOf(gameData.permanentLeaveBatchWatchers),
                Map.copyOf(gameData.permanentLeaveBatchWatcherControllers),
                Map.copyOf(gameData.permanentLeaveBatchPendingCreatures));
        triggerCollectionService.checkSelfOrAllyCreatureLeavesBattlefieldWithoutDyingBatchTriggers(
                gameData, Map.copyOf(gameData.permanentLeaveBatchWatchers),
                Map.copyOf(gameData.permanentLeaveBatchWatcherControllers),
                Map.copyOf(gameData.permanentLeaveBatchPendingCreatures));
        triggerCollectionService.checkAllyPermanentsLeaveBattlefieldBatchTriggers(
                gameData, Map.copyOf(gameData.permanentLeaveBatchWatchers),
                Map.copyOf(gameData.permanentLeaveBatchWatcherControllers),
                Map.copyOf(gameData.permanentLeaveBatchPendingPermanents));
        gameData.permanentLeaveBatchWatchers.clear();
        gameData.permanentLeaveBatchWatcherControllers.clear();
        gameData.permanentLeaveBatchPendingCreatures.clear();
        gameData.permanentLeaveBatchPendingPermanents.clear();
    }

    private void notifyCreatureLeftWithoutDying(GameData gameData, Permanent leavingPermanent,
                                                boolean wasCreature, UUID controllerId) {
        if (!wasCreature || controllerId == null) {
            return;
        }
        if (gameData.permanentLeaveNotificationDepth > 0) {
            gameData.permanentLeaveBatchPendingCreatures.put(leavingPermanent.getId(), controllerId);
            return;
        }
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldWithoutDyingTriggers(
                gameData, leavingPermanent, true, controllerId);
        triggerCollectionService.checkSelfOrAllyCreatureLeavesBattlefieldWithoutDyingTriggers(
                gameData, leavingPermanent, true, controllerId);
    }

    /**
     * Removes a permanent from the battlefield and puts its card into the owner's graveyard.
     * Applies exile replacement effects (CR 614.6), fires death and graveyard triggers for
     * creatures and artifacts, and handles sacrifice-on-unattach and exile-return-on-leave.
     *
     * @param gameData the current game state
     * @param target   the permanent to remove
     * @return {@code true} if the permanent was found on a battlefield and removed,
     *         {@code false} if it was not on any battlefield
     */
    public boolean removePermanentToGraveyard(GameData gameData, Permanent target) {
        return removePermanentToGraveyard(gameData, target, false);
    }

    /** Preserves battlefield information while a simultaneous group of removals is performed. */
    public void performSimultaneousRemovals(GameData gameData, List<Permanent> permanents, Runnable removal) {
        Map<UUID, Permanent> oldPermanents = new java.util.HashMap<>(gameData.simultaneousDyingPermanents);
        Map<UUID, UUID> oldControllers = new java.util.HashMap<>(gameData.simultaneousDyingPermanentControllers);
        Map<UUID, Permanent> oldCreatures = new java.util.HashMap<>(gameData.simultaneousDyingCreatures);
        Map<UUID, UUID> oldCreatureControllers = new java.util.HashMap<>(gameData.simultaneousDyingControllers);
        Map<UUID, Integer> oldPowers = new java.util.HashMap<>(gameData.simultaneousDyingPowers);
        Map<UUID, List<CardEffect>> oldGranted = new java.util.HashMap<>(gameData.simultaneousDyingGrantedCreatureDeathEffects);
        Map<UUID, List<CardEffect>> oldGrantedSelfDeath = new java.util.HashMap<>(gameData.simultaneousDyingGrantedSelfDeathEffects);
        try {
            for (Permanent permanent : permanents) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
                if (controllerId == null) continue;
                Permanent snapshot = new Permanent(permanent);
                snapshot.setLosesAllAbilitiesUntilEndOfTurn(gameQueryService.hasLostPrintedAbilities(gameData, permanent));
                snapshot.setLastKnownPower(gameQueryService.getEffectivePower(gameData, permanent));
                snapshot.setLastKnownToughness(gameQueryService.getEffectiveToughness(gameData, permanent));
                snapshot.setCard(snapshotEffectivePermanentCard(gameData, permanent));
                snapshotEffectiveSubtypes(gameData, snapshot);
                gameData.simultaneousDyingPermanents.put(permanent.getId(), snapshot);
                gameData.simultaneousDyingPermanentControllers.put(permanent.getId(), controllerId);
                gameData.simultaneousDyingGrantedSelfDeathEffects.put(permanent.getId(),
                        List.copyOf(triggerCollectionService.grantedTriggeredEffects(
                                gameData, permanent, EffectSlot.ON_DEATH)));
                if (!gameQueryService.isCreature(gameData, permanent)) continue;
                gameData.simultaneousDyingCreatures.put(permanent.getId(), snapshot);
                gameData.simultaneousDyingControllers.put(permanent.getId(), controllerId);
                gameData.simultaneousDyingPowers.put(permanent.getId(), snapshot.getLastKnownPower());
                gameData.simultaneousDyingGrantedCreatureDeathEffects.put(permanent.getId(),
                        List.copyOf(triggerCollectionService.grantedTriggeredEffects(
                                gameData, permanent, EffectSlot.ON_ANY_CREATURE_DIES)));
            }
            removal.run();
            triggerCollectionService.checkBatchedAllyArtifactOrCreatureDeathTriggers(gameData);
            triggerCollectionService.checkBatchedAllyCreatureDeathTriggers(gameData);
        } finally {
            gameData.simultaneousDyingPermanents.clear();
            gameData.simultaneousDyingPermanents.putAll(oldPermanents);
            gameData.simultaneousDyingPermanentControllers.clear();
            gameData.simultaneousDyingPermanentControllers.putAll(oldControllers);
            gameData.simultaneousDyingCreatures.clear();
            gameData.simultaneousDyingCreatures.putAll(oldCreatures);
            gameData.simultaneousDyingControllers.clear();
            gameData.simultaneousDyingControllers.putAll(oldCreatureControllers);
            gameData.simultaneousDyingPowers.clear();
            gameData.simultaneousDyingPowers.putAll(oldPowers);
            gameData.simultaneousDyingGrantedCreatureDeathEffects.clear();
            gameData.simultaneousDyingGrantedCreatureDeathEffects.putAll(oldGranted);
            gameData.simultaneousDyingGrantedSelfDeathEffects.clear();
            gameData.simultaneousDyingGrantedSelfDeathEffects.putAll(oldGrantedSelfDeath);
        }
    }

    public boolean sacrificePermanentToGraveyard(GameData gameData, Permanent target) {
        if (!gameQueryService.triggeredAbilityCanMoveCreatureToken(gameData, target)) {
            return false;
        }
        if (gameQueryService.cantBeAffectedByOwnEffects(
                gameData, target, gameData.currentlyResolvingControllerId)) {
            return false;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        boolean wasNoncreatureArtifact = gameQueryService.isArtifact(gameData, target)
                && !gameQueryService.isCreature(gameData, target);
        boolean removed = removePermanentToGraveyard(gameData, target, false, false, null, true);
        if (removed && wasNoncreatureArtifact && controllerId != null) {
            triggerCollectionService.checkAnyNoncreatureArtifactSacrificedOrDestroyedTriggers(
                    gameData, controllerId, target.getCard());
        }
        return removed;
    }

    /**
     * Moves a permanent to its graveyard as the result of destruction after the caller has
     * applied destruction-specific replacement checks such as indestructible and regeneration.
     */
    public boolean destroyPermanentToGraveyard(GameData gameData, Permanent target) {
        if (gameQueryService.cantBeAffectedByOwnEffects(
                gameData, target, gameData.currentlyResolvingControllerId)) {
            return false;
        }
        return destroyPermanentToGraveyard(gameData, target, true);
    }

    /** Moves a permanent destroyed by a state-based action to its graveyard. */
    public boolean destroyPermanentByStateBasedAction(GameData gameData, Permanent target) {
        return destroyPermanentToGraveyard(gameData, target, false);
    }

    private boolean destroyPermanentToGraveyard(GameData gameData, Permanent target,
                                                boolean destroyedBySpellOrAbility) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        boolean wasNoncreatureArtifact = gameQueryService.isArtifact(gameData, target)
                && !gameQueryService.isCreature(gameData, target);
        boolean removed = removePermanentToGraveyard(gameData, target, destroyedBySpellOrAbility);
        if (removed && wasNoncreatureArtifact && controllerId != null) {
            triggerCollectionService.checkAnyNoncreatureArtifactSacrificedOrDestroyedTriggers(
                    gameData, controllerId, target.getCard());
        }
        return removed;
    }

    public boolean removePermanentToGraveyardAfterDecliningLibraryReplacement(GameData gameData,
                                                                                Permanent target) {
        return removePermanentToGraveyard(gameData, target, false, true);
    }

    private boolean removePermanentToGraveyard(GameData gameData, Permanent target,
                                               boolean destroyedBySpellOrAbility) {
        return removePermanentToGraveyard(gameData, target, destroyedBySpellOrAbility, false);
    }

    private boolean removePermanentToGraveyard(GameData gameData, Permanent target,
                                               boolean destroyedBySpellOrAbility,
                                               boolean ignoreMayLibraryReplacement) {
        return removePermanentToGraveyard(gameData, target, destroyedBySpellOrAbility,
                ignoreMayLibraryReplacement, null);
    }

    /** Moves a permanent into an explicitly named player's graveyard, retaining normal removal processing. */
    public boolean removePermanentToPlayerGraveyard(GameData gameData, Permanent target, UUID destinationPlayerId) {
        return removePermanentToGraveyard(gameData, target, false, false, destinationPlayerId);
    }

    private boolean removePermanentToGraveyard(GameData gameData, Permanent target,
                                               boolean destroyedBySpellOrAbility,
                                               boolean ignoreMayLibraryReplacement, UUID destinationPlayerId) {
        return removePermanentToGraveyard(gameData, target, destroyedBySpellOrAbility,
                ignoreMayLibraryReplacement, destinationPlayerId, false);
    }

    private boolean removePermanentToGraveyard(GameData gameData, Permanent target,
                                               boolean destroyedBySpellOrAbility,
                                               boolean ignoreMayLibraryReplacement, UUID destinationPlayerId,
                                               boolean wasSacrificed) {
        markDynamicToken(gameData, target);
        // Replacement effect: exile instead of going to graveyard (CR 614.6)
        if (tryApplyExileReplacementEffect(gameData, target, true, "going to the graveyard")) {
            return true;
        }

        if (tryApplyDyingCreatureReturnToHandReplacement(gameData, target)) {
            return true;
        }

        if (!ignoreMayLibraryReplacement && offerMayLibraryReplacement(gameData, target)) {
            return false;
        }

        // Capture unattach-sacrifice info before removal
        UUID sacrificeOnUnattachCreatureId = getSacrificeOnUnattachCreatureId(target);

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        boolean modifiedAtDeath = wasCreature && gameQueryService.isModified(gameData, target);
        boolean wasLand = gameQueryService.isLand(gameData, target);
        int dyingPowerAtDeath = wasCreature
                ? gameData.simultaneousDyingPowers.getOrDefault(target.getId(),
                        gameQueryService.getEffectivePower(gameData, target))
                : 0;
        Permanent dyingSnapshot = gameData.simultaneousDyingPermanents.get(target.getId());
        int dyingToughnessAtDeath = wasCreature
                ? dyingSnapshot != null && dyingSnapshot.getLastKnownToughness() != null
                ? dyingSnapshot.getLastKnownToughness() : gameQueryService.getEffectiveToughness(gameData, target)
                : 0;
        List<CardEffect> grantedDeathEffects = grantedSelfDeathEffects(gameData, target);
        boolean wasArtifact = gameQueryService.isArtifact(target);
        boolean wasEnchantment = gameQueryService.isEnchantment(gameData, target);
        Set<CardSubtype> creatureSubtypesAtDeath = wasCreature
                ? effectiveCreatureSubtypesAtDeath(gameData, target)
                : Set.of();
        boolean hadUndying = wasCreature && gameQueryService.hasKeyword(gameData, target, Keyword.UNDYING);
        int persistInstances = wasCreature ? countPersistInstances(gameData, target) : 0;
        boolean creatureDeathTriggersSuppressed = gameQueryService.areCreatureDeathTriggersSuppressed(gameData, target);
        boolean selfGraveyardTriggerSuppressed = selfGraveyardTriggerSuppressed(gameData, target);
        snapshotEffectiveSubtypes(gameData, target);
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = destinationPlayerId == null ? removed.get().ownerId() : destinationPlayerId;

        if (!creatureDeathTriggersSuppressed) {
            triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.GRAVEYARD);
            triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
            triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
            triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
            triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
            triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(
                    gameData, target, wasCreature, controllerId);
            triggerCollectionService.checkAnotherPermanentLeavesBattlefieldTriggers(gameData, target);
            triggerCollectionService.checkAllyPermanentLeavesBattlefieldTriggers(gameData, target, controllerId);
            triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
            triggerCollectionService.checkAllyPermanentLeavesBattlefieldDuringControllerTurnTriggers(
                    gameData, target, controllerId);
            triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
            triggerCollectionService.checkAnotherNontokenArtifactPutIntoGraveyardOrExileFromBattlefieldTriggers(
                    gameData, target, controllerId, Zone.GRAVEYARD);
        }
        processGraveyardAndTriggers(gameData, target, wasCreature, modifiedAtDeath, wasArtifact, wasEnchantment,
                wasLand, creatureSubtypesAtDeath, hadUndying, persistInstances, controllerId, ownerId,
                destroyedBySpellOrAbility, grantedDeathEffects, dyingPowerAtDeath,
                dyingToughnessAtDeath, selfGraveyardTriggerSuppressed, creatureDeathTriggersSuppressed,
                wasSacrificed, removed.get().hadPrintedAbilities());
        handleSacrificeOnUnattach(gameData, target, sacrificeOnUnattachCreatureId);
        handleExileReturnOnLeave(gameData, target, controllerId, removed.get().hadPrintedAbilities());
        return true;
    }

    /** Uses the abilities present before the first member of a simultaneous event left the battlefield. */
    private List<CardEffect> grantedSelfDeathEffects(GameData gameData, Permanent permanent) {
        List<CardEffect> snapshot = gameData.simultaneousDyingGrantedSelfDeathEffects.get(permanent.getId());
        return snapshot != null ? snapshot
                : triggerCollectionService.grantedTriggeredEffects(gameData, permanent, EffectSlot.ON_DEATH);
    }

    private Set<CardSubtype> effectiveCreatureSubtypesAtDeath(GameData gameData, Permanent permanent) {
        Permanent subtypeSource = gameData.simultaneousDyingPermanents.getOrDefault(
                permanent.getId(), permanent);
        Set<CardSubtype> subtypes = new java.util.HashSet<>(
                gameQueryService.effectiveCreatureSubtypes(gameData, subtypeSource));
        for (CardSubtype subtype : CardSubtype.values()) {
            if (gameQueryService.isCreatureSubtype(subtype)
                    && gameQueryService.hasEffectiveSubtype(gameData, subtypeSource, subtype)) {
                subtypes.add(subtype);
            }
        }
        return Set.copyOf(subtypes);
    }

    private void snapshotEffectiveSubtypes(GameData gameData, Permanent permanent) {
        Set<CardSubtype> subtypes = java.util.EnumSet.noneOf(CardSubtype.class);
        for (CardSubtype subtype : CardSubtype.values()) {
            if (gameQueryService.hasEffectiveSubtype(gameData, permanent, subtype)) {
                subtypes.add(subtype);
            }
        }
        permanent.setLastKnownSubtypes(Set.copyOf(subtypes));
    }

    private boolean tryApplyDyingCreatureReturnToHandReplacement(GameData gameData, Permanent target) {
        if (!gameQueryService.isCreature(gameData, target)) {
            return false;
        }

        UUID dyingCreatureControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (dyingCreatureControllerId == null) {
            return false;
        }

        boolean dyingCreatureIsEnchanted = gameQueryService.isEnchanted(gameData, target);
        List<Card> leavingCards = new ArrayList<>(target.cardsLeavingBattlefield());
        for (UUID sourceControllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent source : battlefield) {
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof DyingCreatureReturnToHandReplacementEffect replacement)
                            || !replacement.appliesTo(source, target, dyingCreatureIsEnchanted,
                            sourceControllerId, dyingCreatureControllerId)) {
                        continue;
                    }

                    UUID ownerId = gameData.stolenCreatures.getOrDefault(
                            target.getId(), dyingCreatureControllerId);
                    boolean removed = removePermanentToHand(gameData, target);
                    if (removed && replacement.revealsReturnedCardUntilOwnerNextTurn()) {
                        markCardsRevealedInHandUntilOwnerNextTurn(gameData, ownerId, leavingCards);
                    }
                    if (removed && replacement.preventsPlayingReturnedCardUntilOwnerNextTurn()) {
                        markCardsUnplayableInHandUntilOwnerNextTurn(gameData, ownerId, leavingCards);
                    }
                    return removed;
                }
            }
        }
        return false;
    }

    private void markCardsRevealedInHandUntilOwnerNextTurn(GameData gameData, UUID ownerId,
                                                            List<Card> cards) {
        List<Card> hand = gameData.playerHands.get(ownerId);
        if (hand == null) {
            return;
        }
        for (Card card : cards) {
            if (hand.stream().anyMatch(handCard -> handCard.getId().equals(card.getId()))) {
                gameData.cardsRevealedInHandUntilOwnerNextTurn.put(card.getId(), ownerId);
            }
        }
    }

    private void markCardsUnplayableInHandUntilOwnerNextTurn(GameData gameData, UUID ownerId,
                                                              List<Card> cards) {
        List<Card> hand = gameData.playerHands.get(ownerId);
        if (hand == null) {
            return;
        }
        for (Card card : cards) {
            if (hand.stream().anyMatch(handCard -> handCard.getId().equals(card.getId()))) {
                gameData.cardsCantBePlayedInHandUntilOwnerNextTurn.put(card.getId(), ownerId);
            }
        }
    }

    /**
     * Processes a permanent that has already been removed from the battlefield list by the caller
     * (e.g. via iterator or index-based removal) and sends it to the owner's graveyard.
     * Performs all the same cleanup as {@link #removePermanentToGraveyard(GameData, Permanent)},
     * but skips the list removal step.
     *
     * <p>Use this for state-based actions or combat damage where the caller manages list iteration.
     *
     * @param gameData     the current game state
     * @param target       the permanent that was already removed from the battlefield list
     * @param controllerId the player who controlled the permanent on the battlefield
     */
    public void processAlreadyRemovedToGraveyard(GameData gameData, Permanent target, UUID controllerId) {
        ZoneChangeCounterSupport.preserve(gameData, target);
        snapshotChosenPermanentStats(gameData, target,
                gameQueryService.getEffectivePower(gameData, target),
                gameQueryService.getEffectiveToughness(gameData, target));
        // Replacement effect: exile instead of going to graveyard (CR 614.6)
        if (tryApplyExileReplacementEffect(gameData, target, true, "going to the graveyard")) {
            return;
        }

        unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, target, target.getAttachedTo(), controllerId);
        UUID sacrificeOnUnattachCreatureId = getSacrificeOnUnattachCreatureId(target);

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        boolean modifiedAtDeath = wasCreature && gameQueryService.isModified(gameData, target, controllerId);
        boolean wasLand = gameQueryService.isLand(gameData, target);
        int dyingPowerAtDeath = wasCreature ? gameQueryService.getEffectivePower(gameData, target) : 0;
        int dyingToughnessAtDeath = wasCreature ? gameQueryService.getEffectiveToughness(gameData, target) : 0;
        List<CardEffect> grantedDeathEffects = grantedSelfDeathEffects(gameData, target);
        boolean wasArtifact = gameQueryService.isArtifact(target);
        boolean wasEnchantment = gameQueryService.isEnchantment(gameData, target);
        Set<CardSubtype> creatureSubtypesAtDeath = wasCreature
                ? effectiveCreatureSubtypesAtDeath(gameData, target)
                : Set.of();
        boolean hadUndying = wasCreature && gameQueryService.hasKeyword(gameData, target, Keyword.UNDYING);
        int persistInstances = wasCreature ? countPersistInstances(gameData, target) : 0;
        boolean creatureDeathTriggersSuppressed = gameQueryService.areCreatureDeathTriggersSuppressed(gameData, target);
        boolean selfGraveyardTriggerSuppressed = selfGraveyardTriggerSuppressed(gameData, target);
        RemovedPermanentInfo info = processRemovalCleanup(gameData, target, controllerId, wasCreature, wasLand,
                hadPrintedAbilitiesBeforeRemoval(gameData, target));

        if (!creatureDeathTriggersSuppressed) {
            triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.GRAVEYARD);
            triggerCollectionService.checkSelfLeavesTriggered(gameData, target, info.controllerId());
            triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
            triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
            triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
            triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(
                    gameData, target, wasCreature, info.controllerId());
            triggerCollectionService.checkAnotherPermanentLeavesBattlefieldTriggers(gameData, target);
            triggerCollectionService.checkAllyPermanentLeavesBattlefieldTriggers(gameData, target, info.controllerId());
            triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, info.controllerId());
            triggerCollectionService.checkAllyPermanentLeavesBattlefieldDuringControllerTurnTriggers(
                    gameData, target, info.controllerId());
            triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, info.controllerId());
            triggerCollectionService.checkAnotherNontokenArtifactPutIntoGraveyardOrExileFromBattlefieldTriggers(
                    gameData, target, info.controllerId(), Zone.GRAVEYARD);
        }
        processGraveyardAndTriggers(gameData, target, wasCreature, modifiedAtDeath, wasArtifact, wasEnchantment,
                wasLand, creatureSubtypesAtDeath, hadUndying, persistInstances, info.controllerId(), info.ownerId(), false,
                grantedDeathEffects, dyingPowerAtDeath, dyingToughnessAtDeath, selfGraveyardTriggerSuppressed,
                creatureDeathTriggersSuppressed, false, info.hadPrintedAbilities());
        handleSacrificeOnUnattach(gameData, target, sacrificeOnUnattachCreatureId);
        handleExileReturnOnLeave(gameData, target, info.controllerId(), info.hadPrintedAbilities());
    }

    /**
     * Removes a permanent from the battlefield and returns its card to the owner's hand (bounce).
     * Applies exile replacement effects (CR 614.6) and handles exile-return-on-leave.
     *
     * @param gameData the current game state
     * @param target   the permanent to bounce
     * @return {@code true} if the permanent was found on a battlefield and removed,
     *         {@code false} if it was not on any battlefield
     */
    public boolean removePermanentToHand(GameData gameData, Permanent target) {
        return removePermanentToHand(gameData, target, false);
    }

    /** Reports whether a bounce occurred when a caller needs a conditional return rider. */
    public boolean removePermanentToHand(GameData gameData, Permanent target, boolean requireReturnToHand) {
        boolean dynamicToken = markDynamicToken(gameData, target);
        // Replacement effect: exile instead of going to hand (CR 614.6)
        if (tryApplyExileReplacementEffect(gameData, target, false, "returning to hand")) {
            return !requireReturnToHand;
        }

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        UUID controllerIdBeforeRemoval = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerIdBeforeRemoval = resolvePermanentOwner(gameData, target, controllerIdBeforeRemoval);
        boolean commanderChoice = gameData.isCommander(target.getCard().getId());
        if (!commanderChoice) {
        triggerCollectionService.checkControllerCreatureReturnedToHandTriggers(
                gameData, target, wasCreature, ownerIdBeforeRemoval);
        triggerCollectionService.checkControllerAnotherNonlandPermanentReturnedToHandTriggers(
                gameData, target, controllerIdBeforeRemoval);
        triggerCollectionService.checkControllerPermanentReturnedToHandTriggers(gameData, ownerIdBeforeRemoval);
        }
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        if (!commanderChoice) triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.HAND);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId, Zone.HAND);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(
                gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherPermanentLeavesBattlefieldTriggers(gameData, target);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        notifyCreatureLeftWithoutDying(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldDuringControllerTurnTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        if (!dynamicToken) {
            for (Card leaving : target.cardsLeavingBattlefield()) {
                gameData.addCardToHand(ownerOfCard(leaving, ownerId), leaving);
            }
            Card companion = detachWerewhatCompanion(gameData, target);
            if (companion != null) {
                gameData.addCardToHand(ownerOfCard(companion, ownerId), companion);
            }
        } else {
            clearDynamicToken(gameData, target);
        }
        if (commanderChoice) gameData.commanderBounceContexts.put(target.getCard().getId(),
                new com.github.laxika.magicalvibes.model.CommanderBounceContext(target, controllerId, ownerId, wasCreature));
        else gameData.playersWhoReceivedPermanentFromBattlefieldToHandThisTurn.add(ownerId);
        forgetDepartedPermanentState(gameData, target);
        handleExileReturnOnLeave(gameData, target, controllerId, removed.get().hadPrintedAbilities());
        if (!commanderChoice) triggerCollectionService.checkPermanentReturnedToHandTriggers(gameData, ownerId, target);
        target.setAttachedTo(null);
        return true;
    }

    /** Removes a permanent from the battlefield and puts its card into its owner's command zone. */
    public boolean removePermanentToCommandZone(GameData gameData, Permanent target) {
        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.COMMAND);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId, Zone.COMMAND);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(
                gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherPermanentLeavesBattlefieldTriggers(gameData, target);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        notifyCreatureLeftWithoutDying(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldDuringControllerTurnTriggers(
                gameData, target, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        for (Card leaving : target.cardsLeavingBattlefield()) {
            gameData.playerCommandZones.computeIfAbsent(ownerId, ignored -> new ArrayList<>()).add(leaving);
            triggerCollectionService.checkYourCommanderPutIntoCommandZoneTriggers(
                    gameData, leaving, ownerId, target);
        }
        forgetDepartedPermanentState(gameData, target);
        handleExileReturnOnLeave(gameData, target, controllerId, removed.get().hadPrintedAbilities());
        target.setAttachedTo(null);
        return true;
    }

    /**
     * Removes a permanent from the battlefield without putting its card into another zone.
     * Spellmorph uses this while the card is being cast from the battlefield onto the stack.
     * Like a bounce or library move, this is a non-dying departure.
     */
    public boolean removePermanentToStack(GameData gameData, Permanent target) {
        return removePermanentWithoutDestination(gameData, target, Zone.STACK);
    }

    /** Removes a card for an outside-game transfer without inventing an intervening zone move. */
    public boolean removePermanentToOutsideGame(GameData gameData, Permanent target) {
        return removePermanentWithoutDestination(gameData, target, Zone.OUTSIDE_GAME);
    }

    private boolean removePermanentWithoutDestination(GameData gameData, Permanent target, Zone destination) {
        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        UUID sacrificeOnUnattachCreatureId = getSacrificeOnUnattachCreatureId(target);
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, destination);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId, destination);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(
                gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherPermanentLeavesBattlefieldTriggers(gameData, target);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        notifyCreatureLeftWithoutDying(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldDuringControllerTurnTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        forgetDepartedPermanentState(gameData, target);
        handleSacrificeOnUnattach(gameData, target, sacrificeOnUnattachCreatureId);
        handleExileReturnOnLeave(gameData, target, controllerId, removed.get().hadPrintedAbilities());
        target.setAttachedTo(null);
        return true;
    }

    /**
     * Removes a permanent from the battlefield and puts its card into the owner's exile zone.
     * Handles sacrifice-on-unattach and exile-return-on-leave.
     *
     * @param gameData the current game state
     * @param target   the permanent to exile
     * @return {@code true} if the permanent was found on a battlefield and removed,
     *         {@code false} if it was not on any battlefield
     */
    public boolean removePermanentToExile(GameData gameData, Permanent target) {
        return removePermanentToExile(gameData, target, null);
    }

    /** Removes a permanent to a face-down exile zone. */
    public boolean removePermanentToExileFaceDown(GameData gameData, Permanent target) {
        return removePermanentToExile(gameData, target, null, true);
    }

    /**
     * Removes a permanent to exile and records its cards as exiled with {@code sourcePermanentId}
     * when one is supplied.
     */
    public boolean removePermanentToExile(GameData gameData, Permanent target, UUID sourcePermanentId) {
        return removePermanentToExile(gameData, target, sourcePermanentId, false);
    }

    /**
     * Removes a craft material permanent to exile and marks its leave-the-battlefield triggers as
     * occurring while activating a craft ability.
     */
    public boolean removePermanentToExileAsCraftMaterial(GameData gameData, Permanent target,
                                                         UUID sourcePermanentId) {
        return removePermanentToExile(gameData, target, sourcePermanentId, false, true);
    }

    private boolean removePermanentToExile(GameData gameData, Permanent target, UUID sourcePermanentId,
                                           boolean faceDown) {
        return removePermanentToExile(gameData, target, sourcePermanentId, faceDown, false);
    }

    private boolean removePermanentToExile(GameData gameData, Permanent target, UUID sourcePermanentId,
                                            boolean faceDown,
                                            boolean exiledWhileActivatingCraftAbility) {
        if (!gameQueryService.triggeredAbilityCanMoveCreatureToken(gameData, target)) {
            return false;
        }
        boolean dynamicToken = markDynamicToken(gameData, target);
        if (gameQueryService.cantBeAffectedByOwnEffects(
                gameData, target, gameData.currentlyResolvingControllerId)) {
            return false;
        }
        // Capture unattach-sacrifice info before removal
        UUID sacrificeOnUnattachCreatureId = getSacrificeOnUnattachCreatureId(target);
        List<Card> leavingCards = new ArrayList<>(target.cardsLeavingBattlefield());

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        int exiledPowerAtTrigger = wasCreature
                ? Math.max(0, gameQueryService.getEffectivePower(gameData, target))
                : 0;
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        Card werewhatCompanion = detachWerewhatCompanion(gameData, target);
        if (werewhatCompanion != null) {
            leavingCards.add(werewhatCompanion);
        }
        if (wasCreature) {
            gameData.creatureExileCountThisTurn.merge(controllerId, 1, Integer::sum);
        }
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId, Zone.EXILE,
                exiledWhileActivatingCraftAbility);
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.EXILE);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(
                gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherPermanentLeavesBattlefieldTriggers(gameData, target);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        notifyCreatureLeftWithoutDying(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldDuringControllerTurnTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAnotherNontokenArtifactPutIntoGraveyardOrExileFromBattlefieldTriggers(
                gameData, target, controllerId, Zone.EXILE);
        triggerCollectionService.checkAnyArtifactExiledFromBattlefieldTriggers(gameData, target, controllerId);
        if (!dynamicToken) {
            for (Card leaving : leavingCards) {
                boolean companion = werewhatCompanion != null
                        && werewhatCompanion.getId().equals(leaving.getId());
                UUID leavingOwnerId = companion ? ownerOfCard(leaving, ownerId) : ownerId;
                if (sourcePermanentId == null || companion) {
                    if (faceDown) {
                        exileService.exileCardFaceDown(gameData, leavingOwnerId, leaving, null);
                    } else {
                        exileService.exileCard(gameData, leavingOwnerId, leaving);
                    }
                } else {
                    if (faceDown) {
                        exileService.exileCardFaceDown(gameData, leavingOwnerId, leaving, sourcePermanentId);
                    } else {
                        exileService.exileCard(gameData, leavingOwnerId, leaving, sourcePermanentId);
                    }
                }
            }
        } else {
            clearDynamicToken(gameData, target);
        }
        List<Card> creatureCards = leavingCards.stream()
                .filter(card -> !isToken(gameData, card) && card.hasType(CardType.CREATURE))
                .toList();
        graveyardService.notifyCardsExiledFromBattlefield(
                gameData, leavingCards.size(), controllerId, wasCreature, creatureCards);
        triggerCollectionService.checkAllyCreatureExiledFromBattlefieldTriggers(
                gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnyCreatureExiledFromBattlefieldTriggers(
                gameData, target, wasCreature, controllerId, exiledPowerAtTrigger);
        triggerCollectionService.checkControllerSpellOrAbilityExilesPermanentTriggers(
                gameData, target, controllerId, exilingControllerId(gameData));
        forgetDepartedPermanentState(gameData, target);
        handleSacrificeOnUnattach(gameData, target, sacrificeOnUnattachCreatureId);
        handleExileReturnOnLeave(gameData, target, controllerId, removed.get().hadPrintedAbilities());
        return true;
    }

    /**
     * Exiles a permanent that entered through unearth and immediately returns its card to its
     * owner's hand.
     */
    public boolean removeUnearthedPermanentToHand(GameData gameData, Permanent target) {
        List<Card> leavingCards = new ArrayList<>(target.cardsLeavingBattlefield());
        if (!removePermanentToExile(gameData, target)) {
            return false;
        }
        for (Card leavingCard : leavingCards) {
            ExiledCardEntry exiled = gameData.findExiledCard(leavingCard.getId());
            if (exiled == null) {
                continue;
            }
            gameData.removeFromExile(leavingCard.getId());
            gameData.addCardToHand(exiled.ownerId(), exiled.card());
            triggerCollectionService.checkPermanentReturnedToHandTriggers(gameData, exiled.ownerId());
        }
        return true;
    }

    /**
     * Removes a permanent from the battlefield and puts its card on top of the owner's library.
     * Applies exile replacement effects (CR 614.6) and handles exile-return-on-leave.
     *
     * @param gameData the current game state
     * @param target   the permanent to tuck
     * @return {@code true} if the permanent was found on a battlefield and removed,
     *         {@code false} if it was not on any battlefield
     */
    public boolean removePermanentToLibraryTop(GameData gameData, Permanent target) {
        return removePermanentToLibraryTop(gameData, target, false);
    }

    /**
     * Removes a permanent from the battlefield and puts its card on top of the owner's library,
     * optionally shuffling that owner's library afterwards ("… then that player shuffles their
     * library", Void Stalker).
     *
     * @param gameData the current game state
     * @param target   the permanent to tuck
     * @param shuffle  whether the owner shuffles their library after the card is placed
     * @return {@code true} if the permanent was found on a battlefield and removed,
     *         {@code false} if it was not on any battlefield
     */
    public boolean removePermanentToLibraryTop(GameData gameData, Permanent target, boolean shuffle) {
        boolean dynamicToken = markDynamicToken(gameData, target);
        // Replacement effect: exile instead of going to library (CR 614.6)
        if (tryApplyExileReplacementEffect(gameData, target, false, "going to the library")) {
            return true;
        }

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        Card werewhatCompanion = detachWerewhatCompanion(gameData, target);
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.LIBRARY);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId, Zone.LIBRARY);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(
                gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherPermanentLeavesBattlefieldTriggers(gameData, target);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        notifyCreatureLeftWithoutDying(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldDuringControllerTurnTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        if (!dynamicToken) {
            if (target.isMergedByMutation()) {
                putMutationComponentsInLibrary(gameData, target, ownerId, false, 0, !shuffle);
            } else {
                for (Card leaving : target.cardsLeavingBattlefield()) {
                    gameData.playerDecks.get(ownerId).add(0, leaving);
                }
            }
            if (werewhatCompanion != null) {
                gameData.playerDecks.get(ownerOfCard(werewhatCompanion, ownerId)).add(0, werewhatCompanion);
            }
        } else {
            clearDynamicToken(gameData, target);
        }
        triggerCollectionService.checkCardsPutIntoLibraryTriggers(
                gameData, ownerId, (target.isMergedByMutation()
                        ? (int) target.cardsLeavingBattlefield().stream().filter(card -> !isToken(gameData, card)).count()
                        : target.cardsLeavingBattlefield().size())
                        + (werewhatCompanion == null ? 0 : 1));
        forgetDepartedPermanentState(gameData, target);
        handleExileReturnOnLeave(gameData, target, controllerId, removed.get().hadPrintedAbilities());
        if (shuffle) {
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        }
        return true;
    }

    /**
     * Removes a permanent from the battlefield and puts its card on the bottom of the owner's library.
     * Applies exile replacement effects (CR 614.6) and handles exile-return-on-leave.
     *
     * @param gameData the current game state
     * @param target   the permanent to tuck
     * @return {@code true} if the permanent was found on a battlefield and removed,
     *         {@code false} if it was not on any battlefield
     */
    public boolean removePermanentToLibraryBottom(GameData gameData, Permanent target) {
        boolean dynamicToken = markDynamicToken(gameData, target);
        // Replacement effect: exile instead of going to library (CR 614.6)
        if (tryApplyExileReplacementEffect(gameData, target, false, "going to the library")) {
            return true;
        }

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        Card werewhatCompanion = detachWerewhatCompanion(gameData, target);
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.LIBRARY);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId, Zone.LIBRARY);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(
                gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherPermanentLeavesBattlefieldTriggers(gameData, target);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        notifyCreatureLeftWithoutDying(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldDuringControllerTurnTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        if (!dynamicToken) {
            if (target.isMergedByMutation()) {
                putMutationComponentsInLibrary(gameData, target, ownerId, true, null, true);
            } else {
                for (Card leaving : target.cardsLeavingBattlefield()) {
                    gameData.playerDecks.get(ownerId).add(leaving);
                }
            }
            if (werewhatCompanion != null) {
                gameData.playerDecks.get(ownerOfCard(werewhatCompanion, ownerId)).add(werewhatCompanion);
            }
        } else {
            clearDynamicToken(gameData, target);
        }
        triggerCollectionService.checkCardsPutIntoLibraryTriggers(
                gameData, ownerId, (target.isMergedByMutation()
                        ? (int) target.cardsLeavingBattlefield().stream().filter(card -> !isToken(gameData, card)).count()
                        : target.cardsLeavingBattlefield().size())
                        + (werewhatCompanion == null ? 0 : 1));
        forgetDepartedPermanentState(gameData, target);
        handleExileReturnOnLeave(gameData, target, controllerId, removed.get().hadPrintedAbilities());
        return true;
    }

    /**
     * Puts every permanent in {@code permanents} on the bottom of its owner's library, then runs
     * the one mandatory {@link #removeOrphanedAuras} pass the whole sweep needs.
     *
     * <p>Collect the list before calling: this exists so bulk tucks (a filtered board sweep, a
     * Lich's Mirror reset) don't each re-derive the loop and forget the aura cleanup at the end.
     *
     * @return the permanents that were actually on a battlefield and moved, in the given order
     */
    public List<Permanent> removeAllToLibraryBottom(GameData gameData, List<Permanent> permanents) {
        List<Permanent> moved = new ArrayList<>();
        beginPermanentLeaveBatch(gameData);
        try {
            for (Permanent perm : permanents) {
                if (removePermanentToLibraryBottom(gameData, perm)) {
                    moved.add(perm);
                }
            }
        } finally {
            endPermanentLeaveBatch(gameData);
        }
        removeOrphanedAuras(gameData);
        return moved;
    }

    /**
     * Removes a permanent from the battlefield and puts its card at the specified position
     * from the top of the owner's library (0-indexed: 0 = top, 1 = second, 2 = third, etc.).
     * If the library has fewer cards than the position, the card is placed on the bottom.
     * Applies exile replacement effects (CR 614.6) and handles exile-return-on-leave.
     *
     * @param gameData the current game state
     * @param target   the permanent to tuck
     * @param position 0-indexed position from the top of the library
     * @return {@code true} if the permanent was found on a battlefield and removed,
     *         {@code false} if it was not on any battlefield
     */
    public boolean removePermanentToLibraryPosition(GameData gameData, Permanent target, int position) {
        boolean dynamicToken = markDynamicToken(gameData, target);
        // Replacement effect: exile instead of going to library (CR 614.6)
        if (tryApplyExileReplacementEffect(gameData, target, false, "going to the library")) {
            return true;
        }

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        Card werewhatCompanion = detachWerewhatCompanion(gameData, target);
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.LIBRARY);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId, Zone.LIBRARY);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(
                gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherPermanentLeavesBattlefieldTriggers(gameData, target);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        notifyCreatureLeftWithoutDying(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldDuringControllerTurnTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        if (!dynamicToken) {
            if (target.isMergedByMutation()) {
                putMutationComponentsInLibrary(gameData, target, ownerId, false, position, true);
            } else {
                List<Card> library = gameData.playerDecks.get(ownerId);
                int insertIndex = Math.min(position, library.size());
                for (Card leaving : target.cardsLeavingBattlefield()) {
                    library.add(Math.min(insertIndex, library.size()), leaving);
                    insertIndex++;
                }
            }
            if (werewhatCompanion != null) {
                List<Card> companionLibrary = gameData.playerDecks.get(
                        ownerOfCard(werewhatCompanion, ownerId));
                companionLibrary.add(Math.min(position, companionLibrary.size()), werewhatCompanion);
            }
        } else {
            clearDynamicToken(gameData, target);
        }
        triggerCollectionService.checkCardsPutIntoLibraryTriggers(
                gameData, ownerId, (target.isMergedByMutation()
                        ? (int) target.cardsLeavingBattlefield().stream().filter(card -> !isToken(gameData, card)).count()
                        : target.cardsLeavingBattlefield().size())
                        + (werewhatCompanion == null ? 0 : 1));
        handleExileReturnOnLeave(gameData, target, controllerId, removed.get().hadPrintedAbilities());
        return true;
    }

    /**
     * Removes a permanent from the battlefield and shuffles its card into the owner's library.
     * Applies exile replacement effects (CR 614.6) and handles exile-return-on-leave.
     *
     * @param gameData the current game state
     * @param target   the permanent to shuffle away
     * @return {@code true} if the permanent was found on a battlefield and removed,
     *         {@code false} if it was not on any battlefield
     */
    public boolean removePermanentToLibraryShuffled(GameData gameData, Permanent target) {
        boolean dynamicToken = markDynamicToken(gameData, target);
        UUID libraryOwnerId = gameData.defaultControllerOf(target.getId());
        // Replacement effect: exile instead of going to library (CR 614.6)
        if (tryApplyExileReplacementEffect(gameData, target, false, "going to the library")) {
            LibraryShuffleHelper.shuffleLibrary(gameData, libraryOwnerId);
            return true;
        }

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        Card werewhatCompanion = detachWerewhatCompanion(gameData, target);
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.LIBRARY);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(
                gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherPermanentLeavesBattlefieldTriggers(gameData, target);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        notifyCreatureLeftWithoutDying(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAllyPermanentLeavesBattlefieldDuringControllerTurnTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        int cardsPutIntoLibrary = 0;
        if (!dynamicToken) {
            for (Card leaving : target.cardsLeavingBattlefield()) {
                if (isToken(gameData, leaving)) continue;
                gameData.playerDecks.get(ownerId).add(leaving);
                cardsPutIntoLibrary++;
            }
            if (werewhatCompanion != null && !isToken(gameData, werewhatCompanion)) {
                gameData.playerDecks.get(ownerOfCard(werewhatCompanion, ownerId)).add(werewhatCompanion);
                cardsPutIntoLibrary++;
            }
        } else {
            clearDynamicToken(gameData, target);
        }
        triggerCollectionService.checkCardsPutIntoLibraryTriggers(
                gameData, ownerId, cardsPutIntoLibrary);
        LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        handleExileReturnOnLeave(gameData, target, controllerId, removed.get().hadPrintedAbilities());
        return true;
    }

    /**
     * Finds a card that has already left the battlefield and shuffles it into its owner's library.
     * This is used by triggered abilities whose source may leave before resolution.
     */
    public boolean shuffleCardIntoOwnerLibrary(GameData gameData, Card card, UUID fallbackOwnerId) {
        if (card == null || isToken(gameData, card)) {
            return false;
        }
        UUID ownerId = card.getOwnerId() != null ? card.getOwnerId() : fallbackOwnerId;
        List<Card> library = ownerId == null ? null : gameData.playerDecks.get(ownerId);
        if (library == null) {
            return false;
        }

        boolean removed = false;
        for (List<Card> zone : gameData.playerDecks.values()) {
            removed |= zone.removeIf(candidate -> candidate.getId().equals(card.getId()));
        }
        for (List<Card> zone : gameData.playerHands.values()) {
            removed |= zone.removeIf(candidate -> candidate.getId().equals(card.getId()));
        }
        for (List<Card> zone : gameData.playerGraveyards.values()) {
            removed |= zone.removeIf(candidate -> candidate.getId().equals(card.getId()));
        }
        for (List<Card> zone : gameData.playerCommandZones.values()) {
            removed |= zone.removeIf(candidate -> candidate.getId().equals(card.getId()));
        }
        removed |= gameData.removeFromExile(card.getId());
        if (!removed) {
            return false;
        }

        library.add(card);
        triggerCollectionService.checkCardsPutIntoLibraryTriggers(gameData, ownerId, 1);
        LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        return true;
    }

    /**
     * Removes all auras whose enchanted permanent is no longer on the battlefield.
     *
     * @param gameData the current game state
     * @return {@code true} if any attachment changed (the SBA loop must re-check)
     */
    public boolean removeOrphanedAuras(GameData gameData) {
        if (gameData.effectResolutionDepth > 0 || gameData.deferPlayerLossCheck) {
            return false;
        }
        var result = auraAttachmentService.removeOrphanedAuras(gameData);
        for (var removal : result.removals()) {
            handleExileReturnOnLeave(gameData, removal.permanent(), removal.controllerId(), true);
            triggerCollectionService.checkSelfLeavesTriggered(gameData, removal.permanent(), removal.controllerId());
            triggerCollectionService.collectDeathTrigger(gameData, removal.card(), removal.controllerId(), false);
            triggerCollectionService.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gameData, removal.card(), removal.controllerId());
            triggerCollectionService.checkAnyPermanentPutIntoGraveyardTriggers(gameData, removal.permanent(),
                    removal.controllerId(), removal.controllerId());
        }
        return result.anyChange();
    }

    /**
     * State-based attachment legality (CR 704.5n/704.5q): puts illegally attached auras into
     * their owners' graveyards and unattaches illegally attached equipment.
     *
     * @return {@code true} if any attachment changed (the SBA loop must re-check)
     */
    public boolean enforceAttachmentLegality(GameData gameData) {
        var result = auraAttachmentService.enforceAttachmentLegality(gameData);
        for (var removal : result.removals()) {
            handleExileReturnOnLeave(gameData, removal.permanent(), removal.controllerId(), true);
            triggerCollectionService.checkSelfLeavesTriggered(gameData, removal.permanent(), removal.controllerId());
            triggerCollectionService.collectDeathTrigger(gameData, removal.card(), removal.controllerId(), false);
            triggerCollectionService.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gameData, removal.card(), removal.controllerId());
            triggerCollectionService.checkAnyPermanentPutIntoGraveyardTriggers(gameData, removal.permanent(),
                    removal.controllerId(), removal.controllerId());
        }
        return result.anyChange();
    }

    /**
     * Attempts to destroy a permanent, respecting indestructible and regeneration.
     * If destroyed, the permanent is sent to the graveyard and orphaned auras are cleaned up.
     *
     * @param gameData the current game state
     * @param target   the permanent to destroy
     * @return {@code true} if the permanent was destroyed, {@code false} if it survived
     *         (indestructible or regenerated)
     */
    public boolean tryDestroyPermanent(GameData gameData, Permanent target) {
        return tryDestroyPermanent(gameData, target, false);
    }

    /**
     * Attempts to destroy a permanent, respecting indestructible and optionally bypassing
     * regeneration (e.g. "destroy target creature. It can't be regenerated.").
     * If destroyed, the permanent is sent to the graveyard and orphaned auras are cleaned up.
     *
     * @param gameData            the current game state
     * @param target              the permanent to destroy
     * @param cannotBeRegenerated if {@code true}, regeneration shields are ignored
     * @return {@code true} if the permanent was destroyed, {@code false} if it survived
     */
    public boolean tryDestroyPermanent(GameData gameData, Permanent target, boolean cannotBeRegenerated) {
        return tryDestroyPermanent(gameData, target, cannotBeRegenerated, true);
    }

    /** Allows a resolving effect to reattach an Aura before orphaned attachments are cleaned up. */
    public boolean tryDestroyPermanent(GameData gameData, Permanent target, boolean cannotBeRegenerated,
                                       boolean cleanUpAttachments) {
        if (gameQueryService.cantBeAffectedByOwnEffects(
                gameData, target, gameData.currentlyResolvingControllerId)) {
            return false;
        }
        if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
            gameLogService.append(gameData, GameLog.isIndestructible(target.getCard()));
            log.info("Game {} - {} is indestructible, destroy prevented", gameData.id, target.getCard().getName());
            return false;
        }
        if (damagePreventionService.replaceDestructionWithShieldCounter(target)) {
            return false;
        }
        if (graveyardService.tryReplaceDestruction(gameData, target, !cannotBeRegenerated)) {
            return false;
        }
        destroyPermanentToGraveyard(gameData, target);
        if (cleanUpAttachments) {
            removeOrphanedAuras(gameData);
        }
        return true;
    }

    /**
     * Drains and performs all scheduled {@link DelayedPermanentAction}s of the given kind, in
     * insertion order. Permanents that already left the battlefield are skipped. Exile, sacrifice
     * and return-to-hand clean up orphaned auras after each removal; destruction goes through
     * {@link #tryDestroyPermanent} (which does its own aura cleanup) so indestructible and
     * regeneration still apply, and logs only when the permanent actually died.
     */
    public void processDelayedPermanentActions(GameData gameData, DelayedPermanentActionKind kind) {
        List<DelayedPermanentAction> actions =
                gameData.drainDelayedActions(DelayedPermanentAction.class,
                        a -> a.kind() == kind
                                && (a.followsPermanentController()
                                ? gameQueryService.findPermanentById(gameData, a.permanentId()) == null
                                    || gameData.activePlayerId.equals(gameQueryService.findPermanentController(
                                            gameData, a.permanentId()))
                                : a.controllerId() == null || a.controllerId().equals(gameData.activePlayerId)));
        for (DelayedPermanentAction action : actions) {
            if (kind == DelayedPermanentActionKind.EXILE_TOKEN_AT_NEXT_CLEANUP) {
                resolveDelayedPermanentAction(gameData, action);
                continue;
            }
            Permanent permanent = gameQueryService.findPermanentById(gameData, action.permanentId());
            if (permanent == null) {
                continue;
            }
            UUID controllerId = action.sacrificingPlayerId() != null ? action.sacrificingPlayerId()
                    : action.controllerId() != null ? action.controllerId()
                    : gameQueryService.findPermanentController(gameData, permanent.getId());
            StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                    permanent.getCard(), controllerId,
                    permanent.getCard().getName() + "'s delayed ability",
                    List.of(new com.github.laxika.magicalvibes.model.effect.ResolveDelayedPermanentActionEffect(action)),
                    null, permanent.getId());
            trigger.setSourcePermanentSnapshot(new Permanent(permanent));
            gameData.stack.add(trigger);
        }
    }

    /** Resolves one previously collected delayed action on a permanent. */
    public void resolveDelayedPermanentAction(GameData gameData, DelayedPermanentAction action) {
        DelayedPermanentActionKind kind = action.kind();
        Permanent perm = gameQueryService.findPermanentById(gameData, action.permanentId());
        if (perm == null) {
            return;
        }
        if ((kind.op() == DelayedPermanentActionKind.Op.EXILE
                || kind.op() == DelayedPermanentActionKind.Op.SACRIFICE)
                && !gameQueryService.delayedTriggeredAbilityCanMoveCreatureToken(gameData, perm)) {
            return;
        }
        boolean completed = true;
        switch (kind.op()) {
            case EXILE -> {
                Card exiledCard = perm.getOriginalCard();
                completed = removePermanentToExile(gameData, perm);
                if (completed && kind == DelayedPermanentActionKind.EXILE_WARPED_AT_END_STEP) {
                    ExiledCardEntry exiled = gameData.findExiledCard(exiledCard.getId());
                    if (exiled != null) {
                        gameData.queueDelayedAction(new GrantExilePlayPermissionAtNextTurn(
                                exiled.card().getId(), exiled.ownerId(), exiled.exiledTurnNumber()));
                    }
                }
            }
            case SACRIFICE -> {
                UUID sacrificeControllerId = gameQueryService.findPermanentController(gameData, perm.getId());
                if (action.sacrificingPlayerId() != null
                        && !action.sacrificingPlayerId().equals(sacrificeControllerId)) {
                    return;
                }
                if (gameQueryService.cantBeSacrificed(gameData, perm)) {
                    return;
                }
                boolean sacrificed = sacrificePermanentToGraveyard(gameData, perm);
                if (sacrificed && sacrificeControllerId != null) {
                    triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                            gameData, sacrificeControllerId, perm.getCard());
                }
                if (sacrificed) {
                    returnExiledCardToBattlefield(gameData, action.returnExiledCardId());
                }
            }
            case RETURN_TO_HAND -> removePermanentToHand(gameData, perm);
            case RETURN_TO_COMMAND_ZONE -> removePermanentToCommandZone(gameData, perm);
            case PUT_ON_TOP_OF_LIBRARY -> removePermanentToLibraryTop(gameData, perm);
            case DESTROY -> {
                if (!tryDestroyPermanent(gameData, perm, action.cannotBeRegenerated())) {
                    return;
                }
            }
        }
        if (!completed) {
            return;
        }
        gameLogService.append(gameData,
                GameLog.builder().card(perm.getCard()).text(kind.logSuffix()).build());
        log.info("Game {} - {}{}", gameData.id, perm.getCard().getName(), kind.logSuffix());
        if (kind.op() != DelayedPermanentActionKind.Op.DESTROY) {
            removeOrphanedAuras(gameData);
        }

    }

    private void returnExiledCardToBattlefield(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return;
        }
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled == null || !gameData.removeFromExile(cardId)) {
            return;
        }
        Card card = exiled.card();
        UUID ownerId = exiled.ownerId();
        Permanent permanent = new Permanent(card);
        permanent.setEnteredFromExile(true);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, permanent);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(ownerId) + " returns ", card,
                " from exile to the battlefield."));
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, card, null, false);
    }

    /**
     * Removes a card from any player's graveyard by its ID and cleans up the
     * creature-death tracking set for the current turn.
     *
     * @param gameData the current game state
     * @param cardId   the ID of the card to remove
     */
    public void removeCardFromGraveyardById(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            Card leaving = graveyard.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
            if (graveyard.removeIf(c -> c.getId().equals(cardId))) {
                Set<UUID> tracked = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.get(playerId);
                if (tracked != null) {
                    tracked.remove(cardId);
                }
                Set<UUID> allTracked = gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.get(playerId);
                if (allTracked != null) {
                    allTracked.remove(cardId);
                }
                gameData.graveyardAdventureCastPermissions.remove(cardId);
                graveyardService.notifyCardLeftGraveyard(gameData, playerId, leaving);
                return;
            }
        }
    }

    public void addCardToHandFromGraveyard(GameData gameData, UUID graveyardOwnerId, UUID handOwnerId,
                                           Card card) {
        graveyardService.addCardToHandFromGraveyard(gameData, graveyardOwnerId, handOwnerId, card);
    }

    /** Removes a card from a graveyard and records that this departure was an exile. */
    public void removeCardFromGraveyardByIdForExile(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            Card leaving = graveyard.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
            if (graveyard.removeIf(c -> c.getId().equals(cardId))) {
                Set<UUID> tracked = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.get(playerId);
                if (tracked != null) {
                    tracked.remove(cardId);
                }
                Set<UUID> allTracked = gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.get(playerId);
                if (allTracked != null) {
                    allTracked.remove(cardId);
                }
                gameData.graveyardAdventureCastPermissions.remove(cardId);
                graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, leaving);
                return;
            }
        }
    }

    /**
     * Checks if the player has an aura with {@link RedirectPlayerDamageToEnchantedCreatureEffect}
     * (e.g. Pariah) and redirects incoming damage to the enchanted creature. Destroys the creature
     * if the redirected damage meets or exceeds its lethal damage threshold.
     *
     * @param gameData   the current game state
     * @param playerId   the player who would receive the damage
     * @param damage     the amount of damage to potentially redirect
     * @param sourceName the name of the damage source (for logging)
     * @return {@code 0} if damage was redirected, or the original damage amount if no redirect applies
     */
    public int redirectPlayerDamageToEnchantedCreature(GameData gameData, UUID playerId, int damage, String sourceName) {
        return redirectPlayerDamageToEnchantedCreature(gameData, playerId, damage, sourceName, false, null);
    }

    public int redirectPlayerDamageToEnchantedCreature(GameData gameData, UUID playerId, int damage, String sourceName, boolean isCombatDamage) {
        return redirectPlayerDamageToEnchantedCreature(gameData, playerId, damage, sourceName, isCombatDamage, null);
    }

    public int redirectPlayerDamageToEnchantedCreature(GameData gameData, UUID playerId, int damage,
                                                       String sourceName, boolean isCombatDamage,
                                                       UUID sourcePermanentId) {
        return redirectPlayerDamageToEnchantedCreature(
                gameData, playerId, damage, sourceName, isCombatDamage, sourcePermanentId, null);
    }

    public int redirectPlayerDamageToEnchantedCreature(GameData gameData, UUID playerId, int damage,
                                                       String sourceName, boolean isCombatDamage,
                                                       UUID sourcePermanentId, Card sourceCard) {
        if (damage <= 0) return damage;
        Permanent target = gameQueryService.findEnchantedCreatureByAuraEffect(gameData, playerId, RedirectPlayerDamageToEnchantedCreatureEffect.class);
        boolean sourceRestrictedRedirect = false;
        if (target == null) {
            target = findControlledPermanentWithDamageRedirect(gameData, playerId, sourcePermanentId, null, sourceCard, false);
            sourceRestrictedRedirect = target != null && gameQueryService.getActiveStaticEffects(gameData, target).stream()
                    .anyMatch(effect -> effect instanceof RedirectPlayerDamageToSelfEffect redirect
                            && redirect.onlyFromUnblockedCreatures());
        }
        if (target == null) return damage;

        return redirectDamageToPermanent(gameData, target, damage, sourceName, isCombatDamage,
                sourcePermanentId, sourceCard, sourceRestrictedRedirect);
    }

    public int redirectPlayerDamageFromMatchingSourceToSelf(GameData gameData, UUID playerId, int damage,
                                                             String sourceName, boolean isCombatDamage,
                                                             UUID sourcePermanentId, Card sourceCard) {
        if (damage <= 0) return damage;
        Permanent target = findControlledPermanentWithDamageRedirect(
                gameData, playerId, sourcePermanentId, null, sourceCard, true);
        if (target == null) return damage;

        return redirectDamageToPermanent(gameData, target, damage, sourceName, isCombatDamage,
                sourcePermanentId, sourceCard, false);
    }

    private int redirectDamageToPermanent(GameData gameData, Permanent target, int damage,
                                          String sourceName, boolean isCombatDamage,
                                          UUID sourcePermanentId, Card sourceCard, boolean sourceRestrictedRedirect) {

        int effectiveDamage = damagePreventionService.applyCreaturePreventionShield(gameData, target, damage, isCombatDamage);
        gameData.recordDamageDealtBySource(sourcePermanentId != null ? sourcePermanentId
                : sourceCard == null ? null : sourceCard.getId(), effectiveDamage);
        gameLogService.append(gameData,
                GameLog.cardThen(target.getCard(), " absorbs " + effectiveDamage + " redirected " + sourceName + " damage."));

        if (sourceRestrictedRedirect) {
            if (effectiveDamage > 0) {
                target.addMarkedDamage(sourcePermanentId, effectiveDamage);
                recordDamageToPermanent(gameData, target.getId(), effectiveDamage, isCombatDamage,
                        sourcePermanentId, sourceCard, sourceName);
                if (gameQueryService.isCreature(gameData, target) && sourcePermanentId != null) {
                    gameData.recordDamageDealtToCreatureBySource(sourcePermanentId, target.getId());
                }
        triggerCollectionService.checkAnyPermanentDealtDamageTriggers(gameData, target, effectiveDamage);
                if (sourcePermanentId != null) {
                    gameData.recordDamageRecipientBySource(sourcePermanentId, target.getId());
                    Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                    if (source != null) {
                        if (gameQueryService.hasKeyword(gameData, source, Keyword.DEATHTOUCH)) {
                            target.setDamagedByDeathtouch(true);
                        }
                        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
                        gameData.recordDamageSourceControlledBy(sourcePermanentId, sourceControllerId);
                        graveyardService.recordCreatureDamagedByPermanent(gameData, sourcePermanentId, target, effectiveDamage);
                        triggerCollectionService.checkDelayedWatchedCreatureDealtDamageByAttackingCreatureTriggers(
                                gameData, source, target, effectiveDamage);
                        triggerCollectionService.checkDealtDamageToCreatureTriggers(
                                gameData, target, effectiveDamage, sourceControllerId);
                    }
                }
            }
            return 0;
        }

        target.addMarkedDamage(sourcePermanentId, effectiveDamage);
        recordDamageToPermanent(gameData, target.getId(), effectiveDamage, isCombatDamage,
                sourcePermanentId, sourceCard, sourceName);
        if (gameQueryService.isCreature(gameData, target) && sourcePermanentId != null) {
            gameData.recordDamageDealtToCreatureBySource(sourcePermanentId, target.getId());
        }

        triggerCollectionService.checkAnyPermanentDealtDamageTriggers(gameData, target, effectiveDamage);
        if (effectiveDamage >= gameQueryService.getLethalDamageThreshold(gameData, target)) {
            if (tryDestroyPermanent(gameData, target)) {
                gameLogService.append(gameData,
                        GameLog.cardThen(target.getCard(), " is destroyed by redirected " + sourceName + " damage."));
            }
        }

        return 0;
    }

    private void recordDamageToPermanent(GameData gameData, UUID permanentId, int amount,
                                         boolean isCombatDamage, UUID sourcePermanentId,
                                         Card sourceCard, String sourceName) {
        UUID sourceId = sourcePermanentId != null
                ? sourcePermanentId
                : sourceCard == null ? null : sourceCard.getId();
        UUID sourceControllerId = sourcePermanentId == null
                ? null : gameQueryService.findPermanentController(gameData, sourcePermanentId);
        if (isCombatDamage) {
            gameData.recordDamageToPermanent(permanentId, amount, sourceId, sourceName, sourceControllerId);
        } else {
            gameData.recordNoncombatDamageToPermanent(permanentId, amount);
            gameData.recordDamageToPermanentFromSource(permanentId, amount, sourceId, sourceName,
                    sourceControllerId);
        }
    }

    private Permanent findControlledPermanentWithDamageRedirect(GameData gameData, UUID playerId,
                                                                  UUID sourcePermanentId, StackEntry sourceEntry,
                                                                  Card sourceCard, boolean sourcePredicateOnly) {
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (gameQueryService.hasLostAllAbilities(gameData, permanent)) {
                continue;
            }
            for (CardEffect effect : gameQueryService.getActiveStaticEffects(gameData, permanent)) {
                if (!(effect instanceof RedirectPlayerDamageToSelfEffect redirect)) continue;
                if (sourcePredicateOnly && redirect.sourcePredicate() == null) continue;
                if (redirect.requiresUntapped() && permanent.isTapped()) continue;
                if (redirect.onlyFromUnblockedCreatures()) {
                    if (permanent.isTapped() || !gameQueryService.isCreature(gameData, permanent)
                            || !damagePreventionService.isUnblockedCreatureSource(gameData, sourcePermanentId)) {
                        continue;
                    }
                }
                if (!gameQueryService.matchesDamageSourcePredicate(
                        gameData, sourceEntry, sourceCard, sourcePermanentId == null
                                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId),
                        redirect.sourcePredicate())) {
                    continue;
                }
                return permanent;
            }
        }
        return null;
    }

    private PlanarCreatureExileReplacement planarCreatureExileReplacement(GameData gameData) {
        if (gameData.planechase == null) {
            return null;
        }
        for (var planar : gameData.planechase.faceUp) {
            ExileCreaturesInsteadOfDyingWithLifeLossEffect effect = planar.getCard()
                    .getEffects(EffectSlot.STATIC).stream()
                    .filter(ExileCreaturesInsteadOfDyingWithLifeLossEffect.class::isInstance)
                    .map(ExileCreaturesInsteadOfDyingWithLifeLossEffect.class::cast)
                    .findFirst().orElse(null);
            if (effect != null) {
                return new PlanarCreatureExileReplacement(effect, planar.getCard());
            }
        }
        return null;
    }

    private record PlanarCreatureExileReplacement(
            ExileCreaturesInsteadOfDyingWithLifeLossEffect effect,
            Card sourceCard) {}

    /**
     * Checks if the target has an exile replacement effect and applies it if so.
     * Returns true if a replacement was applied (caller should return early), false otherwise.
     *
     * @param checkExileInsteadOfDie whether to also check isExileInsteadOfDieThisTurn (for graveyard destinations)
     * @param destinationDescription human-readable description of the original destination (e.g. "going to the graveyard")
     */
    private boolean tryApplyExileReplacementEffect(GameData gameData, Permanent target,
                                                   boolean checkExileInsteadOfDie, String destinationDescription) {
        boolean permanentGraveyardReplacement = checkExileInsteadOfDie
                && !gameQueryService.hasLostPrintedAbilities(gameData, target)
                && (GraveyardService.hasExilePermanentsInsteadOfGraveyardReplacementEffect(target.getCard())
                        || GraveyardService.hasExileInsteadOfGraveyardReplacementEffect(target.getCard()));
        boolean perpetualGraveyardReplacement = !gameQueryService.hasLostAllAbilities(gameData, target)
                && checkExileInsteadOfDie
                && target.getOriginalCard() != null
                && gameData.perpetualExileInsteadOfDyingCardIds.contains(target.getOriginalCard().getId());
        if (!target.isExileIfLeavesBattlefield()
                && !target.isExileIfLeavesBattlefieldUntilEndOfTurn()
                && !(checkExileInsteadOfDie && target.isExileIfDying()
                        && gameQueryService.grantedAbilitySurvivesRemoval(
                                gameData, target, target.getExileIfDyingTimestamp()))
                && !(checkExileInsteadOfDie && target.isExileInsteadOfDieThisTurn())
                && !(checkExileInsteadOfDie && target.getCounterCount(CounterType.FINALITY) > 0)
                && !permanentGraveyardReplacement
                && !perpetualGraveyardReplacement) {
            return false;
        }
        UUID sourcePermanentId = checkExileInsteadOfDie && target.isExileInsteadOfDieThisTurn()
                ? target.getExileInsteadOfDieSourcePermanentId() : null;
        boolean exiled = removePermanentToExile(gameData, target, sourcePermanentId);
        if (exiled) {
            gameLogService.append(gameData,
                    GameLog.cardThen(target.getCard(), " is exiled instead of " + destinationDescription + "."));
            removeOrphanedAuras(gameData);
        }
        return exiled;
    }

    private record RemovedPermanentInfo(UUID controllerId, UUID ownerId, boolean hadPrintedAbilities) {}

    /** Captures a permanent's effective types, subtypes, colors, and keywords before departure. */
    public Card snapshotEffectivePermanentCard(GameData gameData, Permanent permanent) {
        var layerSystemService = layerSystemServiceProvider.getObject();
        var pass = layerSystemService.beginPass(gameData);
        try {
            return snapshotEffectivePermanentCardInPass(gameData, permanent);
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    private Card snapshotEffectivePermanentCardInPass(GameData gameData, Permanent permanent) {
        Card lastKnownCard = permanent.getCard().createRuntimeCopy();
        java.util.EnumSet<CardType> lastKnownTypes = java.util.EnumSet.noneOf(CardType.class);
        if (gameQueryService.isCreature(gameData, permanent)) lastKnownTypes.add(CardType.CREATURE);
        if (gameQueryService.isLand(gameData, permanent)) lastKnownTypes.add(CardType.LAND);
        if (gameQueryService.isArtifact(gameData, permanent)) lastKnownTypes.add(CardType.ARTIFACT);
        if (gameQueryService.isEnchantment(gameData, permanent)) lastKnownTypes.add(CardType.ENCHANTMENT);
        if (gameQueryService.isPlaneswalker(gameData, permanent)) lastKnownTypes.add(CardType.PLANESWALKER);
        if (gameQueryService.isBattle(gameData, permanent)) lastKnownTypes.add(CardType.BATTLE);
        if (gameQueryService.isKindred(gameData, permanent)) lastKnownTypes.add(CardType.KINDRED);
        if (!lastKnownTypes.isEmpty()) {
            lastKnownCard.setType(lastKnownTypes.iterator().next());
            lastKnownCard.setAdditionalTypes(lastKnownTypes);
        }
        List<CardSubtype> lastKnownSubtypes = new ArrayList<>();
        for (CardSubtype subtype : CardSubtype.values()) {
            if (gameQueryService.hasEffectiveSubtype(gameData, permanent, subtype)) {
                lastKnownSubtypes.add(subtype);
            }
        }
        lastKnownCard.setSubtypes(lastKnownSubtypes);
        List<CardColor> lastKnownColors = List.copyOf(gameQueryService.getEffectiveColors(gameData, permanent));
        lastKnownCard.setColors(lastKnownColors);
        lastKnownCard.setColor(lastKnownColors.isEmpty() ? null : lastKnownColors.getFirst());
        var bonus = gameQueryService.computeStaticBonus(gameData, permanent);
        java.util.EnumSet<Keyword> keywords = java.util.EnumSet.noneOf(Keyword.class);
        for (Keyword keyword : Keyword.values()) {
            if (gameQueryService.hasKeyword(permanent, bonus, keyword)) keywords.add(keyword);
        }
        lastKnownCard.setKeywords(keywords);
        return lastKnownCard;
    }

    private boolean hadPrintedAbilitiesBeforeRemoval(GameData gameData, Permanent permanent) {
        Permanent snapshot = gameData.simultaneousDyingPermanents.get(permanent.getId());
        return snapshot == null ? !gameQueryService.hasLostPrintedAbilities(gameData, permanent)
                : !snapshot.isFaceDown() && !snapshot.isLosesAllAbilitiesUntilEndOfTurn();
    }

    /**
     * Finds and removes the given permanent from whatever battlefield it's on, cleans up
     * stolen-creature and permanent-exiled-cards tracking, and returns controller/owner info.
     */
    private Optional<RemovedPermanentInfo> removeFromBattlefield(GameData gameData, Permanent target) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(target)) {
                Permanent removalSnapshot = gameData.simultaneousDyingPermanents.get(target.getId());
                boolean hadPrintedAbilities = hadPrintedAbilitiesBeforeRemoval(gameData, target);
                target.setLastKnownToughness(removalSnapshot != null && removalSnapshot.getLastKnownToughness() != null
                        ? removalSnapshot.getLastKnownToughness() : gameQueryService.getEffectiveToughness(gameData, target));
                target.setLastKnownColors(removalSnapshot != null
                        ? Set.copyOf(removalSnapshot.getCard().getColors())
                        : Set.copyOf(gameQueryService.getEffectiveColors(gameData, target)));
                target.setLastKnownPower(gameData.simultaneousDyingPowers.getOrDefault(
                        target.getId(), gameQueryService.getEffectivePower(gameData, target)));
                final int[] attachmentCounts = {0, 0};
                gameData.forEachPermanent((owner, attachment) -> {
                    if (!target.getId().equals(attachment.getAttachedTo())) return;
                    if (gameQueryService.hasEffectiveSubtype(gameData, attachment, CardSubtype.AURA)) attachmentCounts[0]++;
                    if (gameQueryService.hasEffectiveSubtype(gameData, attachment, CardSubtype.EQUIPMENT)) attachmentCounts[1]++;
                });
                target.setLastKnownAuraCount(attachmentCounts[0]);
                target.setLastKnownEquipmentCount(attachmentCounts[1]);
                Card lastKnownCard = removalSnapshot != null ? removalSnapshot.getCard()
                        : snapshotEffectivePermanentCard(gameData, target);
                for (StackEntry entry : gameData.stack) {
                    for (Permanent attacker : entry.getAttackingPermanentSnapshots()) {
                        if (target.getId().equals(attacker.getId())) {
                            attacker.setLastKnownPower(target.getLastKnownPower());
                        }
                    }
                    if (target.getId().equals(entry.getSourcePermanentId())) {
                        Permanent sourceSnapshot = new Permanent(target);
                        sourceSnapshot.setCard(lastKnownCard);
                        entry.setSourcePermanentSnapshot(sourceSnapshot);
                    }
                    if (entry.getAttachedPermanentSnapshot() != null
                            && target.getId().equals(entry.getAttachedPermanentSnapshot().getId())) {
                        entry.setAttachedPermanentSnapshot(new Permanent(target));
                        entry.setTriggeringPermanentPowerAtTrigger(target.getLastKnownPower());
                        entry.setTriggeringPermanentControllerId(playerId);
                    }
                    if (target.getId().equals(entry.getTargetId())
                            || entry.getDeclaredTargetIds().contains(target.getId())) {
                        entry.rememberLastKnownPermanentCard(target.getId(), lastKnownCard);
                        entry.getLastKnownTargetColors().put(target.getId(),
                                Set.copyOf(gameQueryService.getEffectiveColors(gameData, target)));
                    }
                }
                snapshotBeheldPower(gameData, target);
                snapshotChosenPermanentStats(gameData, target,
                        gameQueryService.getEffectivePower(gameData, target),
                        gameQueryService.getEffectiveToughness(gameData, target));
                boolean wasCreature = gameQueryService.isCreature(gameData, target);
                gameData.updateDelayedControllerSpellCastTriggerSourceSnapshot(
                        target, gameQueryService.getEffectivePower(gameData, target));
                boolean wasLand = gameQueryService.isLand(gameData, target);
                unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, target, target.getAttachedTo(), playerId);
                List<StackEntry> watchingEntries = new ArrayList<>(gameData.stack);
                watchingEntries.addAll(gameData.pendingManaAbilityTriggers);
                if (gameData.pendingEffectResolutionEntry != null
                        && !watchingEntries.contains(gameData.pendingEffectResolutionEntry)) {
                    watchingEntries.add(gameData.pendingEffectResolutionEntry);
                }
                for (StackEntry entry : watchingEntries) {
                    if (target.getId().equals(entry.getTargetId())
                            || entry.getDeclaredTargetIds().contains(target.getId())) {
                        entry.rememberRemovedPermanentController(target.getId(), playerId);
                    }
                    if (target.getId().equals(entry.getAttackedTargetId())) {
                        if (gameQueryService.isBattle(gameData, target)) {
                            entry.setDefendingPlayerId(target.getProtectorPlayerId());
                        } else if (gameQueryService.isPlaneswalker(gameData, target)) {
                            entry.setDefendingPlayerId(playerId);
                        }
                    }
                    entry.getLastKnownPermanentCounters().put(target.getId(),
                            new java.util.EnumMap<>(target.getCounters()));
                    if (target.getId().equals(entry.getSourcePermanentId())) {
                        Permanent sourceSnapshot = new Permanent(target);
                        sourceSnapshot.setCard(lastKnownCard);
                        entry.setSourcePermanentSnapshot(sourceSnapshot);
                        for (CardEffect resolvingEffect : entry.getEffectsToResolve()) {
                            if (resolvingEffect instanceof com.github.laxika.magicalvibes.model.effect.CreateTokenWithAttachedCountCountersEffect attachmentCount) {
                                int[] attached = {0};
                                gameData.forEachPermanent((attachedController, attachedPermanent) -> {
                                    if (target.getId().equals(attachedPermanent.getAttachedTo())
                                            && !attachedPermanent.getId().equals(attachmentCount.excludedAttachedPermanentId())
                                            && (attachedPermanent.getCard().getSubtypes().contains(CardSubtype.AURA)
                                            || attachedPermanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT))) {
                                        attached[0]++;
                                    }
                                });
                                entry.setEventValue(attached[0]);
                            }
                        }
                    }
                    if (target.getId().equals(entry.getTriggeringPermanentId())) {
                        entry.rememberLastKnownPermanentCard(target.getId(), lastKnownCard);
                        entry.getRemovedPermanentControllers().put(target.getId(), playerId);
                        entry.setTriggeringPermanentPowerAtTrigger(
                                gameQueryService.getEffectivePower(gameData, target));
                        entry.setTriggeringPermanentToughnessAtTrigger(
                                gameQueryService.getEffectiveToughness(gameData, target));
                        if (entry.getEffectsToResolve().stream().anyMatch(effect ->
                                effect instanceof com.github.laxika.magicalvibes.model.effect.MayEffect may
                                        && may.choicePlayer() == com.github.laxika.magicalvibes.model.MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER)) {
                            entry.setTriggeringPermanentControllerId(playerId);
                        }
                    }
                }
                battlefield.remove(target);
                ZoneChangeCounterSupport.preserve(gameData, target);
                preserveBlockedStatusWhenBlockerLeaves(gameData, target);
                return Optional.of(processRemovalCleanup(gameData, target, playerId, wasCreature, wasLand, hadPrintedAbilities));
            }
        }
        return Optional.empty();
    }

    private void snapshotBeheldPower(GameData gameData, Permanent target) {
        for (StackEntry entry : gameData.stack) {
            if (target.getId().equals(entry.getBeholdPermanentId())) {
                entry.setBeholdPower(Math.max(0, gameQueryService.getEffectivePower(gameData, target)));
            }
        }
    }

    private void snapshotChosenPermanentStats(GameData gameData, Permanent target, int power, int toughness) {
        for (StackEntry entry : gameData.stack) {
            if (target.getId().equals(entry.getChosenPermanentId())) {
                entry.setChosenPermanentPowerAtLastKnown(Math.max(0, power));
                entry.setChosenPermanentToughnessAtLastKnown(Math.max(0, toughness));
            }
        }
    }

    private void preserveBlockedStatusWhenBlockerLeaves(GameData gameData, Permanent blocker) {
        if (!blocker.isBlocking()) {
            return;
        }
        for (UUID attackerId : blocker.getBlockingTargetIds()) {
            Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
            if (attacker != null && attacker.isAttacking()) {
                attacker.setBlockedWithoutBlockers(true);
            }
        }
    }

    private void forgetDepartedPermanentState(GameData gameData, Permanent departed) {
        UUID cardId = departed.getCard().getId();
        gameData.creaturesReturnedToBattlefieldOnDeathThisTurn.remove(cardId);
        for (Set<UUID> damagedCardIds : gameData.creatureCardsDamagedThisTurnBySourcePermanent.values()) {
            damagedCardIds.remove(cardId);
        }
        for (Set<UUID> damagedCardIds : gameData.creatureCardsDamagedThisTurnBySource.values()) {
            damagedCardIds.remove(cardId);
        }
    }

    /**
     * Performs all leaving-the-battlefield cleanup for a permanent that has already been removed
     * from the battlefield list. This is the single point where structural cleanup happens.
     */
    private RemovedPermanentInfo processRemovalCleanup(
            GameData gameData, Permanent target, UUID controllerId, boolean wasCreature, boolean wasLand,
            boolean hadPrintedAbilities) {
        notifyPermanentLeftBattlefield(gameData, target, controllerId);
        gameData.playersWhosePermanentsLeftBattlefieldThisTurn.add(controllerId);
        if (!wasLand) {
            gameData.nonlandPermanentLeftBattlefieldThisTurn = true;
        }
        if (wasCreature) {
            gameData.creatureLeftBattlefieldCountThisTurn.merge(controllerId, 1, Integer::sum);
        }
        target.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(AnimateNoncreatureArtifactsEffect.class::isInstance)
                .map(AnimateNoncreatureArtifactsEffect.class::cast)
                .filter(AnimateNoncreatureArtifactsEffect::losesAllAbilities)
                .findFirst()
                .ifPresent(effect -> gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), target.getCard().getName(), null, controllerId,
                        effect, null, null, new PermanentIsArtifactPredicate(),
                        EffectDuration.UNTIL_END_OF_TURN, 0)));
        UUID ownerId = resolvePermanentOwner(gameData, target, controllerId);
        gameData.stolenCreatures.remove(target.getId());
        // A departing Aura ends the layer-1 copy it granted (Metamorphic Alteration): its
        // WHILE_ATTACHED floating effect expires here and drives the enchanted creature's revert.
        auraCopyService.revertExpiredCopies(gameData,
                gameData.expireFloatingEffectsForDepartedSource(target.getId()));
        gameData.expireControlEffectsForDepartedPermanent(target.getId());
        gameData.expireExilePlayPermissionsForSource(target.getId());
        creatureControlService.reconcileControl(gameData);
        untapLockReleaseService.releaseUntapLocks(gameData, target);
        handleSourceLinkedAnimationCleanup(gameData, target);
        handlePreparedSpellCleanup(gameData, target);
        clearSoulbondPairing(gameData, target);
        return new RemovedPermanentInfo(controllerId, ownerId, hadPrintedAbilities);
    }

    private void notifyPermanentLeftBattlefield(GameData gameData, Permanent leavingPermanent,
                                                UUID controllerId) {
        if (controllerId == null) {
            return;
        }
        if (gameData.permanentLeaveNotificationDepth > 0) {
            gameData.permanentLeaveBatchPendingPermanents.put(leavingPermanent.getId(), controllerId);
            return;
        }
        triggerCollectionService.checkAllyPermanentsLeaveBattlefieldTriggers(
                gameData, leavingPermanent, controllerId);
    }

    private UUID resolvePermanentOwner(GameData gameData, Permanent permanent, UUID controllerId) {
        UUID trackedOwnerId = gameData.stolenCreatures.get(permanent.getId());
        if (trackedOwnerId != null) {
            return trackedOwnerId;
        }
        UUID cardOwnerId = permanent.getOriginalCard().getOwnerId();
        return cardOwnerId != null ? cardOwnerId : controllerId;
    }

    private void clearSoulbondPairing(GameData gameData, Permanent target) {
        UUID partnerId = target.getPairedWithId();
        if (partnerId == null) {
            return;
        }
        target.setPairedWithId(null);
        Permanent partner = gameQueryService.findPermanentById(gameData, partnerId);
        if (partner != null && target.getId().equals(partner.getPairedWithId())) {
            partner.setPairedWithId(null);
        }
    }

    /**
     * Liesa, Forgotten Archangel: true when a player other than {@code controllerId} controls a
     * permanent with "if a creature an opponent controls would die, exile it instead".
     */
    private OpponentDyingCreatureExileReplacement opponentDyingCreatureExileReplacement(
            GameData gameData, UUID controllerId, Card dyingCard) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                ExileOpponentCreaturesInsteadOfDyingEffect effect = permanent.getCard()
                        .getEffects(EffectSlot.STATIC).stream()
                        .filter(ExileOpponentCreaturesInsteadOfDyingEffect.class::isInstance)
                        .map(ExileOpponentCreaturesInsteadOfDyingEffect.class::cast)
                        .filter(candidate -> !candidate.nontokenOnly() || !isToken(gameData, dyingCard))
                        .findFirst().orElse(null);
                if (effect != null) {
                    return new OpponentDyingCreatureExileReplacement(
                            effect, permanent.getCard(), playerId, permanent.getId());
                }
            }
        }
        for (Map.Entry<UUID, Permanent> entry : gameData.simultaneousDyingPermanents.entrySet()) {
            UUID sourceControllerId = gameData.simultaneousDyingPermanentControllers.get(entry.getKey());
            if (sourceControllerId == null || sourceControllerId.equals(controllerId)) {
                continue;
            }
            Permanent permanent = entry.getValue();
            ExileOpponentCreaturesInsteadOfDyingEffect effect = permanent.getCard()
                    .getEffects(EffectSlot.STATIC).stream()
                    .filter(ExileOpponentCreaturesInsteadOfDyingEffect.class::isInstance)
                    .map(ExileOpponentCreaturesInsteadOfDyingEffect.class::cast)
                        .filter(candidate -> !candidate.nontokenOnly() || !isToken(gameData, dyingCard))
                    .findFirst().orElse(null);
            if (effect != null) {
                return new OpponentDyingCreatureExileReplacement(
                        effect, permanent.getCard(), sourceControllerId, permanent.getId());
            }
        }
        return null;
    }

    private record OpponentDyingCreatureExileReplacement(
            ExileOpponentCreaturesInsteadOfDyingEffect effect,
            Card sourceCard,
            UUID controllerId,
            UUID sourcePermanentId) {}

    private boolean ownSubtypeExileReplacement(GameData gameData, Permanent dyingPermanent,
                                               UUID dyingControllerId,
                                               Set<CardSubtype> dyingSubtypes) {
        if (dyingSubtypes.isEmpty()) {
            return false;
        }

        if (hasOwnSubtypeExileReplacement(dyingPermanent, dyingSubtypes)) {
            return true;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(dyingControllerId);
        if (battlefield != null && battlefield.stream()
                .anyMatch(source -> hasOwnSubtypeExileReplacement(source, dyingSubtypes))) {
            return true;
        }

        for (Map.Entry<UUID, Permanent> entry : gameData.simultaneousDyingPermanents.entrySet()) {
            UUID sourceControllerId = gameData.simultaneousDyingPermanentControllers.get(entry.getKey());
            if (dyingControllerId.equals(sourceControllerId)
                    && hasOwnSubtypeExileReplacement(entry.getValue(), dyingSubtypes)) {
                return true;
            }
        }
        return false;
    }

    private NontokenCreatureDyingExileReplacement nontokenCreatureDyingExileReplacement(
            GameData gameData, Permanent dyingPermanent) {
        if (dyingPermanent != null && dyingPermanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(ExileNontokenCreaturesInsteadOfDyingWithBloodCounterEffect.class::isInstance)) {
            return new NontokenCreatureDyingExileReplacement(
                    dyingPermanent.getCard(), dyingPermanent.getId());
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(ExileNontokenCreaturesInsteadOfDyingWithBloodCounterEffect.class::isInstance)) {
                    return new NontokenCreatureDyingExileReplacement(
                            permanent.getCard(), permanent.getId());
                }
            }
        }
        for (Permanent permanent : gameData.simultaneousDyingPermanents.values()) {
            if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(ExileNontokenCreaturesInsteadOfDyingWithBloodCounterEffect.class::isInstance)) {
                return new NontokenCreatureDyingExileReplacement(
                        permanent.getCard(), permanent.getId());
            }
        }
        return null;
    }

    private record NontokenCreatureDyingExileReplacement(Card sourceCard, UUID sourcePermanentId) {
    }

    private boolean hasOwnSubtypeExileReplacement(Permanent source, Set<CardSubtype> dyingSubtypes) {
        return source.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(ExileOwnCreaturesOfSubtypeInsteadOfDyingEffect.class::isInstance)
                .map(ExileOwnCreaturesOfSubtypeInsteadOfDyingEffect.class::cast)
                .anyMatch(effect -> dyingSubtypes.contains(effect.subtype()));
    }

    /**
     * True when a player other than {@code ownerId} controls a permanent with an opponent-owned
     * creature-card graveyard replacement, and the dying card is not a token.
     */
    private boolean opponentExilesOwnedNontokenCreature(GameData gameData, UUID ownerId, Card card) {
        if (isToken(gameData, card)) {
            return false;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ownerId)) {
                continue;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (gameQueryService.getActiveStaticEffects(gameData, permanent).stream()
                        .anyMatch(OpponentCreatureCardExileReplacement.class::isInstance)) {
                    return true;
                }
            }
        }
        for (Map.Entry<UUID, Permanent> entry : gameData.simultaneousDyingPermanents.entrySet()) {
            UUID controllerId = gameData.simultaneousDyingPermanentControllers.get(entry.getKey());
            if (controllerId != null && !ownerId.equals(controllerId)
                    && !entry.getValue().isFaceDown()
                    && !entry.getValue().isLosesAllAbilitiesUntilEndOfTurn()
                    && entry.getValue().getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(OpponentCreatureCardExileReplacement.class::isInstance)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Frostwielder / Kumano's Blessing: true when a permanent that damaged {@code dying} this turn
     * has "if a creature dealt damage by this creature this turn would die, exile it instead" —
     * printed on the permanent itself, or on an Aura currently attached to it.
     */
    private boolean damagerExilesDyingCreature(GameData gameData, Permanent dying) {
        UUID cardId = dying.getCard().getId();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent source : battlefield) {
                if (!gameData.creatureCardsDamagedThisTurnBySourcePermanent
                        .getOrDefault(source.getId(), Set.of()).contains(cardId)) {
                    continue;
                }
                if (hasExileDamagedCreaturesInsteadOfDying(source)
                        || auraOnSourceExilesDamagedCreatures(gameData, source.getId())) {
                    return true;
                }
            }
        }
        if (controlledSourceExilesDyingCreature(gameData, cardId)) {
            return true;
        }
        for (Permanent source : gameData.simultaneousDyingCreatures.values()) {
            if (gameData.creatureCardsDamagedThisTurnBySourcePermanent
                    .getOrDefault(source.getId(), Set.of()).contains(cardId)
                    && (hasExileDamagedCreaturesInsteadOfDying(source)
                    || auraOnSourceExilesDamagedCreatures(gameData, source.getId()))) {
                return true;
            }
        }
        return false;
    }

    private boolean controlledSourceExilesDyingCreature(GameData gameData, UUID dyingCardId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent effectSource : battlefield) {
                if (effectSource.getCard().getEffects(EffectSlot.STATIC).stream()
                        .noneMatch(ExileCreaturesDamagedByControlledSourceInsteadOfDyingEffect.class::isInstance)) {
                    continue;
                }
                UUID controllerId = gameQueryService.findPermanentController(gameData, effectSource.getId());
                if (controllerId == null) {
                    continue;
                }
                for (UUID sourceId : gameData.damageSourcesControlledByPlayerThisTurn
                        .getOrDefault(controllerId, Set.of())) {
                    if (gameData.creatureCardsDamagedThisTurnBySourcePermanent
                            .getOrDefault(sourceId, Set.of()).contains(dyingCardId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasExileDamagedCreaturesInsteadOfDying(Permanent permanent) {
        return permanent.isExileDamagedCreaturesInsteadOfDyingThisTurn()
                || permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(ExileCreaturesDamagedBySourceInsteadOfDyingEffect.class::isInstance);
    }

    private boolean auraOnSourceExilesDamagedCreatures(GameData gameData, UUID sourceId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent aura : battlefield) {
                if (aura.isAttached()
                        && sourceId.equals(aura.getAttachedTo())
                        && hasExileDamagedCreaturesInsteadOfDying(aura)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sends a removed permanent's card to the graveyard and fires all death/graveyard triggers.
     */
    private void processGraveyardAndTriggers(GameData gameData, Permanent target,
                                              boolean wasCreature, boolean modifiedAtDeath,
                                              boolean wasArtifact,
                                              boolean wasEnchantment,
                                              boolean wasLand,
                                              Set<CardSubtype> creatureSubtypesAtDeath,
                                              boolean hadUndying, int persistInstances,
                                              UUID controllerId, UUID ownerId,
                                              boolean destroyedBySpellOrAbility,
                                              List<CardEffect> grantedDeathEffects,
                                              int dyingPowerAtDeath,
                                              int dyingToughnessAtDeath,
                                              boolean selfGraveyardTriggerSuppressed,
                                              boolean creatureDeathTriggersSuppressed,
                                              boolean wasSacrificed,
                                              boolean hadPrintedAbilitiesAtDeparture) {
        Permanent graveyardSnapshot = new Permanent(target);
        graveyardSnapshot.setLosesAllAbilitiesUntilEndOfTurn(!hadPrintedAbilitiesAtDeparture);
        boolean wentToGraveyard = false;
        int exiledFromBattlefield = 0;
        List<Card> exiledCreatureCards = new ArrayList<>();
        PlanarCreatureExileReplacement planarExileReplacement = wasCreature
                ? planarCreatureExileReplacement(gameData) : null;
        // Disturb back-face (etc.): exile-instead is printed on the current face; the physical
        // card that leaves is still originalCard / meld components.
        NontokenCreatureDyingExileReplacement bloodCounterReplacement = wasCreature
                && !isToken(gameData, target.getCard())
                ? nontokenCreatureDyingExileReplacement(gameData, target)
                : null;
        OpponentDyingCreatureExileReplacement opponentExileReplacement = wasCreature
                && bloodCounterReplacement == null
                ? opponentDyingCreatureExileReplacement(gameData, controllerId, target.getCard())
                : null;
        boolean ownSubtypeExileReplacement = wasCreature
                && ownSubtypeExileReplacement(gameData, target, controllerId, creatureSubtypesAtDeath);
        boolean controlledPermanentExileReplacement = gameData.playersExilingControlledPermanentsInsteadOfDyingThisTurn
                .contains(controllerId);
        boolean exileInstead = GraveyardService.hasExileInsteadOfGraveyardReplacementEffect(target.getCard())
                || opponentExileReplacement != null
                || bloodCounterReplacement != null
                || ownSubtypeExileReplacement
                || (wasCreature && opponentExilesOwnedNontokenCreature(gameData, ownerId, target.getCard()))
                || (wasCreature && damagerExilesDyingCreature(gameData, target))
                || planarExileReplacement != null
                || (wasCreature && !gameData.playersExilingCreaturesInsteadOfDyingThisTurn.isEmpty())
                || (wasCreature && gameData.playersExilingOpponentCreaturesInsteadOfDyingThisTurn.stream()
                        .anyMatch(exilingPlayerId -> !exilingPlayerId.equals(controllerId)))
                || controlledPermanentExileReplacement;
        if (exileInstead && wasCreature) {
            gameData.creatureExileCountThisTurn.merge(controllerId, 1, Integer::sum);
        }
        Card werewhatCompanion = detachWerewhatCompanion(gameData, target);
        List<Card> leavingCards = new ArrayList<>(target.cardsLeavingBattlefield());
        if (werewhatCompanion != null) {
            leavingCards.add(werewhatCompanion);
        }
        int mergedComponentsInGraveyard = 0;
        int mergedArtifactComponentsInGraveyard = 0;
        for (Card leaving : leavingCards) {
            UUID leavingOwnerId = target.isMergedByMutation() || (werewhatCompanion != null
                    && werewhatCompanion.getId().equals(leaving.getId()))
                    ? ownerOfCard(leaving, ownerId) : ownerId;
            if (exileInstead) {
                boolean isBloodCounterExile = bloodCounterReplacement != null
                        && leaving.getId().equals(target.getCard().getId());
                if (isBloodCounterExile) {
                    exileService.exileCard(gameData, leavingOwnerId, leaving,
                            bloodCounterReplacement.sourcePermanentId());
                    gameData.exiledCardsWithBloodCounters.add(leaving.getId());
                } else if (opponentExileReplacement != null
                        && opponentExileReplacement.effect().trackWithSource()) {
                    exileService.exileCard(gameData, leavingOwnerId, leaving,
                            opponentExileReplacement.sourcePermanentId());
                } else {
                    exileService.exileCard(gameData, leavingOwnerId, leaving);
                }
                if (!isToken(gameData, leaving) && leaving.hasType(CardType.CREATURE)) {
                    exiledCreatureCards.add(leaving);
                }
                if (opponentExileReplacement != null && opponentExileReplacement.effect().addIceCounter()) {
                    gameData.exiledCardsWithIceCounters.add(leaving.getId());
                }
                exiledFromBattlefield++;
                gameLogService.append(gameData,
                        isBloodCounterExile
                                ? GameLog.cardThen(leaving,
                                " is exiled with a blood counter instead of being put into a graveyard.")
                                : GameLog.cardThen(leaving,
                                " is exiled instead of being put into a graveyard."));
                if (controlledPermanentExileReplacement
                        && !isToken(gameData, leaving)
                        && gameData.findExiledCard(leaving.getId()) != null) {
                    gameData.queueDelayedAction(new DelayedEndStepTrigger(
                            controllerId, leaving, null, null,
                            new ReturnExiledCardToBattlefieldUnderOwnerControlEffect(leaving.getId())));
                }
                } else {
                    boolean enteredGraveyard = graveyardService.addCardToGraveyard(
                            gameData, leavingOwnerId, leaving, Zone.BATTLEFIELD, controllerId, graveyardSnapshot,
                            selfGraveyardTriggerSuppressed, creatureDeathTriggersSuppressed);
                if (enteredGraveyard) {
                    wentToGraveyard = true;
                    if (target.isMergedByMutation() && target.getMutatedComponentCards().stream()
                            .anyMatch(component -> component.getId().equals(leaving.getId()))) {
                        mergedComponentsInGraveyard++;
                        if (leaving.hasType(CardType.ARTIFACT)) mergedArtifactComponentsInGraveyard++;
                    }
                    if (wasEnchantment) {
                        gameData.playersWhoPutEnchantmentIntoGraveyardFromBattlefieldThisTurn.add(ownerId);
                    }
                } else if (gameData.findExiledCard(leaving.getId()) != null) {
                    exiledFromBattlefield++;
                    if (!isToken(gameData, leaving) && leaving.hasType(CardType.CREATURE)) {
                        exiledCreatureCards.add(leaving);
                    }
                }
            }
        }
        if (target.isMergedByMutation()) {
            if (mergedComponentsInGraveyard > 0) {
                gameData.permanentsPutIntoGraveyardFromBattlefieldThisTurn -= mergedComponentsInGraveyard - 1;
                gameData.artifactsPutIntoGraveyardFromBattlefieldThisTurn -= mergedArtifactComponentsInGraveyard;
                if (wasArtifact) gameData.artifactsPutIntoGraveyardFromBattlefieldThisTurn++;
            }
            queueMutationComponentOrders(gameData, target, ownerId, Zone.GRAVEYARD, null);
        }
        if (opponentExileReplacement != null
                && opponentExileReplacement.effect().lifeGainOnExile() > 0) {
            lifeSupport.applyGainLife(gameData, opponentExileReplacement.controllerId(),
                    opponentExileReplacement.effect().lifeGainOnExile(),
                    opponentExileReplacement.sourceCard().getName());
        }
        if (planarExileReplacement != null && exiledFromBattlefield > 0) {
            lifeSupport.applyLifeLoss(gameData, controllerId,
                    planarExileReplacement.effect().lifeLoss(),
                    planarExileReplacement.sourceCard().getName());
        }
        if (opponentExileReplacement != null && exiledFromBattlefield > 0) {
            CardEffect whenExiledEffect = opponentExileReplacement.effect().whenExiledEffect();
            if (whenExiledEffect instanceof MayPayManaEffect mayPay) {
                gameData.queueMayAbility(
                        opponentExileReplacement.sourceCard(), opponentExileReplacement.controllerId(),
                        mayPay, null, opponentExileReplacement.sourcePermanentId());
            } else if (whenExiledEffect instanceof MayEffect may) {
                gameData.queueMayAbility(
                        opponentExileReplacement.sourceCard(), opponentExileReplacement.controllerId(),
                        may, null, opponentExileReplacement.sourcePermanentId());
            } else if (whenExiledEffect != null) {
                resolveMandatoryOpponentExileRider(gameData, opponentExileReplacement, whenExiledEffect,
                        wasCreature && exiledFromBattlefield > 0, dyingPowerAtDeath);
            }
        }
        graveyardService.notifyCardsExiledFromBattlefield(
                gameData, exiledFromBattlefield, controllerId,
                wasCreature && exiledFromBattlefield > 0, exiledCreatureCards);
        if (exiledFromBattlefield > 0) {
            triggerCollectionService.checkAllyCreatureExiledFromBattlefieldTriggers(
                    gameData, target, wasCreature, controllerId);
            triggerCollectionService.checkAnyCreatureExiledFromBattlefieldTriggers(
                    gameData, target, wasCreature, controllerId, Math.max(0, dyingPowerAtDeath));
            triggerCollectionService.checkControllerSpellOrAbilityExilesPermanentTriggers(
                    gameData, target, controllerId, exilingControllerId(gameData));
        }
        if (wentToGraveyard) {
            triggerCollectionService.checkHauntedCreatureDeathTriggers(gameData, target);
            if (target.getCounterCount(CounterType.OIL) > 0) {
                gameData.recordPermanentWithOilCounterPutIntoGraveyard();
            }
            if (!creatureDeathTriggersSuppressed) {
                triggerCollectionService.collectDeathTrigger(gameData, target.getCard(), controllerId, wasCreature, target,
                        grantedDeathEffects, dyingPowerAtDeath, wasLand);
            }
            // Any permanent an opponent controls is put into a graveyard (Prince of Thralls).
            if (!creatureDeathTriggersSuppressed) {
                triggerCollectionService.checkOpponentPermanentPutIntoGraveyardTriggers(
                        gameData, target.getOriginalCard(), controllerId, ownerId, new Permanent(target));
            }
            // Any permanent owned by another player is put into a graveyard (Kothophed, Soul Hoarder).
            if (!creatureDeathTriggersSuppressed) {
                triggerCollectionService.checkOtherPlayerOwnedPermanentPutIntoGraveyardTriggers(
                        gameData, target.getOriginalCard(), ownerId);
            }
            // "Whenever a creature or planeswalker you control dies" — fires once even when the
            // dying permanent is both (Ajani's Last Stand).
            if ((wasCreature && !creatureDeathTriggersSuppressed)
                    || target.getCard().hasType(CardType.PLANESWALKER)) {
                triggerCollectionService.checkAllyCreatureOrPlaneswalkerDeathTriggers(
                        gameData, controllerId, target, wasCreature);
            }
            // Any permanent at all is put into a graveyard (Yomiji, Who Bars the Way).
            // Retain types granted by battlefield effects for graveyard-event filters.
            Permanent graveyardEventSnapshot = new Permanent(target);
            if (wasCreature) graveyardEventSnapshot.getGrantedCardTypes().add(CardType.CREATURE);
            if (wasArtifact) graveyardEventSnapshot.getGrantedCardTypes().add(CardType.ARTIFACT);
            if (wasEnchantment) graveyardEventSnapshot.getGrantedCardTypes().add(CardType.ENCHANTMENT);
            triggerCollectionService.checkAnyPermanentPutIntoGraveyardTriggers(
                    gameData, graveyardEventSnapshot, controllerId, ownerId, dyingPowerAtDeath, dyingToughnessAtDeath);
            if (wasCreature) {
                gameData.creatureDeathCountThisTurn.merge(controllerId, 1, Integer::sum);
                gameData.creaturePermanentIdsDiedThisTurn.add(target.getId());
                if (modifiedAtDeath) {
                    gameData.playersWhoControlledModifiedCreatureDiedThisTurn.add(controllerId);
                }
                gameData.creatureNamesDiedThisTurn.add(target.getCard().getName());
                gameData.creaturesPutIntoOwnGraveyardThisTurnCount.merge(ownerId, 1, Integer::sum);
                if (!target.getCard().isToken()) {
                    gameData.nontokenCreaturesPutIntoOwnGraveyardThisTurnCount.merge(ownerId, 1, Integer::sum);
                    gameData.nontokenCreatureDeathCountThisTurn.merge(controllerId, 1, Integer::sum);
                }
                Map<CardSubtype, Integer> subtypeCounts = gameData.creatureSubtypeDeathCountThisTurn
                        .computeIfAbsent(controllerId, ignored -> new java.util.concurrent.ConcurrentHashMap<>());
                for (CardSubtype subtype : creatureSubtypesAtDeath) {
                    subtypeCounts.merge(subtype, 1, Integer::sum);
                }
                Map<CardSubtype, Integer> subtypePowers = gameData.creatureSubtypeDeathPowerThisTurn
                        .computeIfAbsent(controllerId, ignored -> new java.util.concurrent.ConcurrentHashMap<>());
                for (CardSubtype subtype : creatureSubtypesAtDeath) {
                    subtypePowers.merge(subtype, dyingPowerAtDeath, Integer::sum);
                }
                if (!creatureDeathTriggersSuppressed) {
                    triggerCollectionService.checkCreaturePutIntoOwnersGraveyardFromBattlefieldTriggers(
                            gameData, target, ownerId, controllerId,
                            dyingPowerAtDeath, dyingToughnessAtDeath);
                    triggerCollectionService.checkAllyCreatureDeathTriggers(
                            gameData, controllerId, target, dyingPowerAtDeath);
                    triggerCollectionService.checkGraveyardAllyCreatureDeathTriggers(gameData, controllerId, target);
                    triggerCollectionService.checkGraveyardOpponentCreatureDeathTriggers(
                            gameData, controllerId, target);
                    triggerCollectionService.checkAnyCreatureDeathTriggers(gameData, controllerId, target);
                    triggerCollectionService.checkAllyNontokenCreatureDeathTriggers(
                            gameData, controllerId, target, dyingPowerAtDeath);
                    triggerCollectionService.checkAnyNontokenCreatureDeathTriggers(
                            gameData, target.getCard(), ownerId);
                    triggerCollectionService.checkEnchantedPlayerNontokenCreatureDeathTriggers(
                            gameData, controllerId, target, dyingPowerAtDeath);
                    triggerCollectionService.checkOpponentCreatureDeathTriggers(
                            gameData, controllerId, target, dyingPowerAtDeath, dyingToughnessAtDeath);
                    triggerCollectionService.checkEquippedCreatureDeathTriggers(
                            gameData, target.getId(), controllerId, target.getCard(), dyingPowerAtDeath,
                            target, creatureSubtypesAtDeath);
                    triggerCollectionService.triggerDelayedPoisonOnDeath(gameData, target.getCard().getId(), controllerId);
                    collectUndyingTrigger(gameData, target, controllerId, hadUndying);
                    collectPersistTriggers(gameData, target, controllerId, persistInstances);
                }
            }
            if (!creatureDeathTriggersSuppressed) {
                triggerCollectionService.triggerDelayedEffectOnDeath(
                        gameData, target.getCard().getId(), controllerId, target.getEffectivePower(),
                        target.getCard().getManaValue(), Map.copyOf(target.getCounters()), target.getId());
                triggerCollectionService.triggerDelayedReturnOnDeath(
                        gameData, target.getCard().getId(), target.getOriginalCard(), ownerId);
            }
            if (wasArtifact && !creatureDeathTriggersSuppressed) {
                triggerCollectionService.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(
                        gameData, ownerId, controllerId, target.getOriginalCard(), target.getCard().getManaValue(),
                        Map.copyOf(target.getCounters()), wasSacrificed);
            }
            if (wasEnchantment && !creatureDeathTriggersSuppressed) {
                triggerCollectionService.checkAnyEnchantmentPutIntoGraveyardFromBattlefieldTriggers(gameData, ownerId, controllerId);
            }
            if (wasLand && !creatureDeathTriggersSuppressed) {
                triggerCollectionService.checkLandPutIntoGraveyardByOpponentTriggers(
                        gameData, target.getOriginalCard(), ownerId, gameData.currentlyResolvingControllerId);
                triggerCollectionService.checkAnyLandPutIntoGraveyardFromBattlefieldTriggers(
                        gameData, target.getCard(), ownerId, controllerId);
            }
            if (destroyedBySpellOrAbility && !wasCreature) {
                UUID destroyingControllerId = gameData.currentlyResolvingControllerId;
                if (destroyingControllerId != null && !destroyingControllerId.equals(controllerId)) {
                    gameData.playersWhoseNoncreaturePermanentsWereDestroyedByOpponentThisTurn.add(controllerId);
                }
                triggerCollectionService.checkNoncreaturePermanentDestroyedByOpponentTriggers(
                        gameData, target, controllerId, gameData.currentlyResolvingControllerId);
            }
            triggerCollectionService.checkEnchantedPermanentDeathTriggers(gameData, target.getId(), controllerId,
                    target.getCard().getId(), dyingPowerAtDeath, dyingToughnessAtDeath,
                    target.getCard().getManaValue(), wasCreature,
                    target.cardsLeavingBattlefield().stream().map(Card::getId).toList());
            // Check if the dying permanent was an Aura or Equipment (Tiana, Ship's Caretaker)
            if ((target.getCard().isAura() || target.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT))
                    && !creatureDeathTriggersSuppressed) {
                triggerCollectionService.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gameData, target.getCard(), controllerId);
            }
        }
    }

    private UUID exilingControllerId(GameData gameData) {
        if (gameData.currentlyResolvingControllerId != null) {
            return gameData.currentlyResolvingControllerId;
        }
        return gameData.pendingEffectResolutionEntry == null
                ? null : gameData.pendingEffectResolutionEntry.getControllerId();
    }

    private void resolveMandatoryOpponentExileRider(
            GameData gameData, OpponentDyingCreatureExileReplacement replacement,
            CardEffect rider, boolean creatureExiled, int dyingPowerAtDeath) {
        if (!creatureExiled) {
            return;
        }
        EffectHandler handler = effectHandlerRegistry.getHandler(rider);
        if (handler == null) {
            log.warn("No handler for mandatory opponent-exile rider: {}", rider.getClass().getSimpleName());
            return;
        }
        StackEntry riderEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                replacement.sourceCard(),
                replacement.controllerId(),
                replacement.sourceCard().getName() + "'s replacement effect",
                new ArrayList<>(List.of(rider)),
                0,
                replacement.sourcePermanentId());
        riderEntry.setEventValue(Math.max(0, dyingPowerAtDeath));
        handler.resolve(gameData, riderEntry, rider);
    }

    private boolean offerMayLibraryReplacement(GameData gameData, Permanent target) {
        DyingCreatureLibraryReplacementEffect replacement = target.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(DyingCreatureLibraryReplacementEffect.class::isInstance)
                .map(DyingCreatureLibraryReplacementEffect.class::cast)
                .filter(DyingCreatureLibraryReplacementEffect::mayChoose)
                .findFirst()
                .orElseGet(() -> {
                    var bonus = gameQueryService.computeStaticBonus(gameData, target);
                    if (bonus == null) {
                        return null;
                    }
                    return bonus.grantedEffects().stream()
                            .filter(DyingCreatureLibraryReplacementEffect.class::isInstance)
                            .map(DyingCreatureLibraryReplacementEffect.class::cast)
                            .filter(DyingCreatureLibraryReplacementEffect::mayChoose)
                            .findFirst()
                            .orElse(null);
                });
        if (replacement == null) {
            return false;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId == null) {
            return false;
        }
        if (gameData.pendingMayAbilities.stream()
                .anyMatch(ability -> target.getId().equals(ability.sourcePermanentId())
                        && ability.effects().stream().anyMatch(PutOnTopOfLibraryInsteadOfDyingEffect.class::isInstance))) {
            return true;
        }
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                target.getCard(),
                controllerId,
                List.of(replacement),
                target.getCard().getName()
                        + " — Put it on top of its owner's library instead of putting it into a graveyard?",
                target.getId(),
                null,
                target.getId()));
        if (!gameData.interaction.isAwaitingInput()) {
            playerInputService.processNextMayAbility(gameData);
        }
        return true;
    }

    private boolean selfGraveyardTriggerSuppressed(GameData gameData, Permanent target) {
        if (target.getCard().getEffects(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD).isEmpty()
                && target.getPersistentTriggeredEffects(
                        EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD).isEmpty()) {
            return false;
        }
        if (target.isLosesAllAbilitiesUntilEndOfTurn()) {
            return true;
        }
        GameQueryService.StaticBonus bonus = gameQueryService.computeStaticBonus(gameData, target);
        return bonus != null && (bonus.losesAllAbilities() || bonus.losesAllNonManaAbilities());
    }

    /**
     * Undying (CR 702.93): when a creature with undying dies, if it had no +1/+1 counters on it, push a
     * triggered ability that returns it from the graveyard to the battlefield with a +1/+1 counter. The
     * "if it had no +1/+1 counters" intervening-if uses the counter count at the moment it died (the
     * permanent has already left the battlefield, so this is last-known information).
     */
    private void collectUndyingTrigger(GameData gameData, Permanent dyingPermanent, UUID controllerId, boolean hadUndying) {
        if (!hadUndying) return;
        if (dyingPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0) return;

        Card dyingCard = dyingPermanent.getOriginalCard();
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                dyingCard,
                controllerId,
                dyingCard.getName() + "'s undying ability",
                new ArrayList<>(List.of(new UndyingReturnEffect()))
        ));
        gameLogService.append(gameData, GameLog.cardThen(dyingCard, "'s undying ability triggers."));
        log.info("Game {} - {} undying triggers", gameData.id, dyingCard.getName());
    }

    /**
     * Persist (CR 702.79): when a creature with persist dies, if it had no -1/-1 counters on it, push a
     * triggered ability that returns it from the graveyard to the battlefield with a -1/-1 counter. The
     * "if it had no -1/-1 counters" intervening-if uses the counter count at the moment it died (the
     * permanent has already left the battlefield, so this is last-known information).
     */
    private int countPersistInstances(GameData gameData, Permanent permanent) {
        if (!gameQueryService.hasKeyword(gameData, permanent, Keyword.PERSIST)) return 0;
        int instances = !gameQueryService.hasLostPrintedAbilities(gameData, permanent)
                && permanent.getCard().getKeywords().contains(Keyword.PERSIST) ? 1 : 0;
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                if (permanent.getId().equals(floating.affectedPermanentId())
                        && floating.effect() instanceof GrantKeywordEffect grant
                        && grant.keywords().contains(Keyword.PERSIST)) {
                    instances++;
                }
            }
        }
        return Math.max(1, instances);
    }

    private void collectPersistTriggers(GameData gameData, Permanent dyingPermanent, UUID controllerId,
                                        int persistInstances) {
        if (persistInstances == 0 || dyingPermanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0) return;
        Card dyingCard = dyingPermanent.getOriginalCard();
        for (int instance = 0; instance < persistInstances; instance++) {
            StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, dyingCard, controllerId,
                    dyingCard.getName() + "'s persist ability", new ArrayList<>(List.of(new PersistReturnEffect())));
            trigger.setTriggeringCardId(dyingCard.getId());
            trigger.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(dyingCard.getId()));
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.cardThen(dyingCard, "'s persist ability triggers."));
        }
    }

    /**
     * Returns the ID of the creature that should be sacrificed if the given permanent is an equipment
     * with SacrificeOnUnattachEffect that is currently attached to a creature. Returns null otherwise.
     */
    private UUID getSacrificeOnUnattachCreatureId(Permanent equipment) {
        if (!equipment.isAttached()) return null;
        if (!equipment.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) return null;
        boolean hasEffect = equipment.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof SacrificeOnUnattachEffect);
        return hasEffect ? equipment.getAttachedTo() : null;
    }

    /**
     * After an equipment with SacrificeOnUnattachEffect is removed from the battlefield,
     * sacrifice the creature it was attached to (if it still exists).
     */
    private void handleSacrificeOnUnattach(GameData gameData, Permanent removedEquipment, UUID creatureId) {
        if (creatureId == null) return;
        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        if (creature == null) return;
        gameLogService.append(gameData, GameLog.builder()
                .card(creature.getCard())
                .text(" is sacrificed (")
                .card(removedEquipment.getCard())
                .text(" became unattached).")
                .build());
        log.info("Game {} - {} sacrificed due to {} leaving battlefield", gameData.id, creature.getCard().getName(), removedEquipment.getCard().getName());
        sacrificePermanentToGraveyard(gameData, creature);
        removeOrphanedAuras(gameData);
    }

    /**
     * Cleans up source-linked animations (Awakener Druid-style) when a permanent leaves the battlefield.
     * If the removed permanent was a source, reverts the target land back to a normal land.
     * If the removed permanent was an animated target, removes the tracking entry.
     */
    private void handleSourceLinkedAnimationCleanup(GameData gameData, Permanent removedPermanent) {
        UUID removedId = removedPermanent.getId();

        // Check if this permanent was a source for any linked animations
        var iterator = gameData.sourceLinkedAnimations.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().equals(removedId)) {
                Permanent animatedTarget = gameQueryService.findPermanentById(gameData, entry.getKey());
                if (animatedTarget != null) {
                    FloatingContinuousEffect remainingAnimation = gameData.floatingEffects.stream()
                            .filter(floating -> animatedTarget.getId().equals(floating.affectedPermanentId())
                                    && floating.duration() == EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD
                                    && floating.effect() instanceof com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect
                                    && floating.sourcePermanentId() != null)
                            .max(java.util.Comparator.comparingLong(FloatingContinuousEffect::timestamp))
                            .orElse(null);
                    if (remainingAnimation != null) {
                        var base = (com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect)
                                remainingAnimation.effect();
                        animatedTarget.setPermanentAnimatedPower(base.power());
                        animatedTarget.setPermanentAnimatedToughness(base.toughness());
                        entry.setValue(remainingAnimation.sourcePermanentId());
                        continue;
                    }
                    animatedTarget.setPermanentlyAnimated(false);
                    animatedTarget.setPermanentAnimatedPower(0);
                    animatedTarget.setPermanentAnimatedToughness(0);
                    animatedTarget.getGrantedSubtypes().clear();
                    animatedTarget.getGrantedColors().clear();

                    gameLogService.append(gameData,
                            GameLog.cardThen(animatedTarget.getCard(), " is no longer a creature."));
                    log.info("Game {} - {} reverts to non-creature (source {} left battlefield)",
                            gameData.id, animatedTarget.getCard().getName(), removedPermanent.getCard().getName());
                }
                iterator.remove();
            }
        }

        // Also clean up if the removed permanent was itself an animated target
        gameData.sourceLinkedAnimations.remove(removedId);
    }

    /**
     * "Prepared" (Secrets of Strixhaven): a prepare-spell copy only exists in exile while its
     * prepared permanent is on the battlefield. When that permanent leaves, the exiled copy ceases
     * to exist and its play permission is removed.
     */
    private void handlePreparedSpellCleanup(GameData gameData, Permanent removedPermanent) {
        if (!removedPermanent.isPrepared()) return;
        UUID prepareCopyId = removedPermanent.getPreparedSpellCardId();
        if (prepareCopyId != null) {
            gameData.removeFromExile(prepareCopyId);
            gameData.exilePlayPermissions.remove(prepareCopyId);
        }
        removedPermanent.setPrepared(false);
        removedPermanent.setPreparedSpellCardId(null);
    }

    /**
     * Checks if the removed permanent had an exile-until-source-leaves tracking entry.
     * If so, returns the exiled card to the battlefield under its owner's control.
     */
    private void handleExileReturnOnLeave(GameData gameData, Permanent removedPermanent, UUID controllerId,
                                        boolean hadPrintedAbilities) {
        gameData.hauntingCardToPermanentId.entrySet()
                .removeIf(entry -> removedPermanent.getId().equals(entry.getValue()));
        List<PendingExileReturn> pendingReturns = gameData.exileReturnOnPermanentLeave.remove(removedPermanent.getId());
        if (pendingReturns == null) {
            triggerCollectionService.processDelayedExileReturnCounterTriggers(
                    gameData, removedPermanent.getId(), List.of(), List.of());
            phasingService.phaseInWhenSourceLeaves(gameData, removedPermanent.getId());
            return;
        }

        boolean returnsThroughLeavesTrigger = java.util.stream.Stream.of(
                        EffectSlot.ON_ENTER_BATTLEFIELD, EffectSlot.ON_TURNED_FACE_UP)
                .flatMap(slot -> removedPermanent.getCard().getEffects(slot).stream()).anyMatch(effect -> effect instanceof ExileTargetCreaturesUntilSourceLeavesEffect exile
                        && exile.returnToHand()
                        || effect instanceof com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect);
        returnsThroughLeavesTrigger |= removedPermanent.getCard().getEffects(EffectSlot.SPELL).stream()
                .anyMatch(com.github.laxika.magicalvibes.model.effect.BeholdAndExileCost.class::isInstance);
        if (returnsThroughLeavesTrigger) {
            if (!hadPrintedAbilities) {
                phasingService.phaseInWhenSourceLeaves(gameData, removedPermanent.getId());
                return;
            }
            List<CardEffect> effects = pendingReturns.stream()
                    .map(pending -> (CardEffect) new ResolvePendingExileReturnEffect(pending,
                            gameData.exileEntryVersions.getOrDefault(pending.card().getId(), 0L)))
                    .toList();
            gameData.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, removedPermanent.getCard(),
                    controllerId, removedPermanent.getCard().getName() + "'s leaves-the-battlefield ability",
                    new ArrayList<>(effects)));
            phasingService.phaseInWhenSourceLeaves(gameData, removedPermanent.getId());
            return;
        }
        List<UUID> returnedPermanentIds = new ArrayList<>();
        for (PendingExileReturn pending : pendingReturns) {
            returnedPermanentIds.add(returnPendingExiledCard(gameData, removedPermanent.getId(), pending));
        }
        triggerCollectionService.processDelayedExileReturnCounterTriggers(
                gameData, removedPermanent.getId(), pendingReturns, returnedPermanentIds);
        phasingService.phaseInWhenSourceLeaves(gameData, removedPermanent.getId());
    }

    /** Resolves only the pending returns captured by one source-untap trigger. */
    public void returnExileReturnsOnSourceEvent(GameData gameData, UUID sourcePermanentId,
                                                Set<UUID> primaryCardIds) {
        List<PendingExileReturn> pendingReturns =
                gameData.exileReturnOnPermanentLeave.get(sourcePermanentId);
        if (pendingReturns == null || primaryCardIds.isEmpty()) {
            return;
        }
        List<PendingExileReturn> captured = new ArrayList<>();
        pendingReturns.removeIf(pending -> {
            if (primaryCardIds.contains(pending.card().getId())) {
                captured.add(pending);
                return true;
            }
            return false;
        });
        if (pendingReturns.isEmpty()) {
            gameData.exileReturnOnPermanentLeave.remove(sourcePermanentId, pendingReturns);
        }
        for (PendingExileReturn pending : captured) {
            returnPendingExiledCard(gameData, sourcePermanentId, pending);
        }
    }

    /** Returns cards exiled until an opponent of the new monarch becomes monarch. */
    public void returnExileReturnsOnOpponentBecomesMonarch(GameData gameData, UUID monarchPlayerId) {
        List<PendingExileReturn> pendingReturns = new ArrayList<>();
        gameData.exileReturnOnOpponentBecomesMonarch.entrySet().removeIf(entry -> {
            if (entry.getKey().equals(monarchPlayerId)) {
                return false;
            }
            pendingReturns.addAll(entry.getValue());
            return true;
        });
        for (PendingExileReturn pending : pendingReturns) {
            returnPendingExiledCard(gameData, null, pending);
        }
    }

    /** Resolves a previously queued linked exile return, using its original source identity. */
    public void resolvePendingExileReturn(GameData gameData, UUID sourcePermanentId,
                                           PendingExileReturn pending) {
        returnPendingExiledCard(gameData, sourcePermanentId, pending);
    }

    private UUID returnPendingExiledCard(GameData gameData, UUID sourcePermanentId,
                                         PendingExileReturn pending) {
        Card exiledCard = pending.card();
        UUID ownerId = pending.controllerId();

        ExiledCardEntry currentExileEntry = gameData.findExiledCard(exiledCard.getId());
        if (pending.returnToGraveyard()
                && (currentExileEntry == null
                || !sourcePermanentId.equals(currentExileEntry.sourcePermanentId()))) {
            log.info("Game {} - Idol-linked card {} no longer exiled with its source, return skipped",
                    gameData.id, exiledCard.getName());
            return null;
        }

        if (gameData.removeFromExile(exiledCard.getId())) {
            String playerName = gameData.playerIdToName.get(ownerId);

            if (pending.returnToGraveyard()) {
                graveyardService.addCardToGraveyard(gameData, ownerId, exiledCard, Zone.EXILE);
                gameLogService.append(gameData,
                        GameLog.cardThen(exiledCard, " returns to " + playerName + "'s graveyard."));
                log.info("Game {} - {} returns to graveyard from exile (source left battlefield)",
                        gameData.id, exiledCard.getName());
            } else if (pending.returnToHand()) {
                // Return to owner's hand (e.g. Kitesail Freebooter — exiled from hand)
                gameData.addCardToHand(ownerId, exiledCard);
                gameLogService.append(gameData,
                        GameLog.cardThen(exiledCard, " returns to " + playerName + "'s hand."));
                log.info("Game {} - {} returns to hand from exile (source left battlefield)", gameData.id, exiledCard.getName());
            } else {
                return returnExiledCardsToBattlefield(gameData, ownerId, pending, exiledCard);
            }
        } else {
            log.info("Game {} - Exiled card {} no longer in exile zone, return skipped", gameData.id, exiledCard.getName());
        }
        return null;
    }

    private UUID returnExiledCardsToBattlefield(GameData gameData, UUID ownerId,
                                                PendingExileReturn pending, Card exiledCard) {
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<Permanent> returnedPermanents = new ArrayList<>();
        List<UUID> returnedControllerIds = new ArrayList<>();
        List<Card> cards = new ArrayList<>();
        cards.add(exiledCard);
        cards.addAll(pending.additionalCards());
        Permanent primaryPermanent = null;

        for (Card card : cards) {
            boolean primary = card.getId().equals(pending.card().getId());
            boolean attachToPrimary = primaryPermanent != null
                    && pending.cardsToAttachToPrimary().contains(card.getId());
            UUID cardOwnerId = ownerOfExiledCard(gameData, card, ownerId);
            if (attachToPrimary
                    && !auraAttachmentService.canEnchant(gameData, card, cardOwnerId,
                    primaryPermanent)) {
                log.info("Game {} - {} stays exiled because it cannot enchant the returned {}",
                        gameData.id, card.getName(), primaryPermanent.getCard().getName());
                continue;
            }
            if (!primary && pending.cardsToAttachToPrimary().contains(card.getId())
                    && primaryPermanent == null) {
                continue;
            }
            if (!primary && !gameData.removeFromExile(card.getId())) {
                continue;
            }

            Permanent permanent = new Permanent(card);
            permanent.setEnteredFromExile(true);
            if (pending.returnTapped()) {
                permanent.tap();
            }
            applyPendingReturnCounters(gameData, ownerId, pending, permanent, primary);
            if (pending.grantHaste()) {
                permanent.getPersistentGrantedKeywords().add(Keyword.HASTE);
            }
            if (primary) {
                primaryPermanent = permanent;
            } else if (attachToPrimary) {
                permanent.setAttachedTo(primaryPermanent.getId());
            }

            UUID controllerId = attachToPrimary ? cardOwnerId : ownerId;
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(permanent);
            returnedPermanents.add(permanent);
            returnedControllerIds.add(controllerId);
            gameLogService.append(gameData,
                    GameLog.cardThen(card, " returns to the battlefield under "
                            + gameData.playerIdToName.get(controllerId) + "'s control."));
            log.info("Game {} - {} returns from exile for {}", gameData.id, card.getName(),
                    gameData.playerIdToName.get(controllerId));
        }

        for (int i = 0; i < returnedPermanents.size(); i++) {
            Card card = returnedPermanents.get(i).getCard();
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, returnedControllerIds.get(i), card, null, false);
        }
        if (primaryPermanent != null && !pending.cardsToAttachToPrimary().isEmpty()) {
            creatureControlService.recomputeControl(gameData, primaryPermanent);
        }
        return primaryPermanent == null ? null : primaryPermanent.getId();
    }

    private void applyPendingReturnCounters(GameData gameData, UUID ownerId,
                                            PendingExileReturn pending, Permanent permanent,
                                            boolean primary) {
        if (pending.plusOnePlusOneCounters() > 0
                && (!pending.plusOnePlusOneCountersOnlyOnCreatures()
                || permanent.getCard().hasType(CardType.CREATURE))
                && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent, ownerId)) {
            int counters = gameQueryService.doublePlusOnePlusOneCounters(
                    gameData, permanent, ownerId, pending.plusOnePlusOneCounters());
            if (counters > 0) {
                permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
            }
        }
        if (permanent.getCard().hasType(CardType.PLANESWALKER)
                && permanent.getCard().getLoyalty() != null) {
            int loyalty = gameQueryService.replaceCounters(gameData, ownerId,
                    CounterType.LOYALTY, permanent.getCard().getLoyalty()
                            + pending.loyaltyCountersOnPlaneswalkers(), permanent.getCard().hasType(CardType.CREATURE));
            permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        }
        if (pending.counterTypeOnReturn() != null && pending.counterAmountOnReturn() > 0) {
            int counters = gameQueryService.replaceCounters(gameData, permanent, ownerId,
                    pending.counterTypeOnReturn(), pending.counterAmountOnReturn());
            if (counters > 0) {
                permanent.setCounterCount(pending.counterTypeOnReturn(), counters);
            }
        }
        if (primary) {
            for (Map.Entry<CounterType, Integer> counter : pending.countersOnReturn().entrySet()) {
                int counters = gameQueryService.replaceCounters(gameData, permanent, ownerId,
                        counter.getKey(), counter.getValue());
                if (counters > 0) {
                    permanent.setCounterCount(counter.getKey(), counters);
                }
            }
        }
    }

    private UUID ownerOfExiledCard(GameData gameData, Card card, UUID fallback) {
        ExiledCardEntry exiledEntry = gameData.findExiledCard(card.getId());
        return exiledEntry == null ? (card.getOwnerId() == null ? fallback : card.getOwnerId())
                : exiledEntry.ownerId();
    }

    private Card detachWerewhatCompanion(GameData gameData, Permanent permanent) {
        Card companion = permanent.getWerewhatCompanionCard();
        if (companion == null) {
            return null;
        }
        ExiledCardEntry entry = gameData.findExiledCard(companion.getId());
        if (entry != null) {
            gameData.removeFromExile(companion.getId());
            companion = entry.card();
        }
        permanent.setWerewhatCompanionCard(null);
        return companion;
    }

    /** Places the physical nontoken components together at the requested library position. */
    private void putMutationComponentsInLibrary(GameData gameData, Permanent target, UUID fallbackOwnerId,
                                                 boolean toBottom, Integer position, boolean chooseOrder) {
        Map<UUID, List<Card>> cardsByOwner = new java.util.LinkedHashMap<>();
        for (Card card : target.cardsLeavingBattlefield()) {
            if (isToken(gameData, card)) continue;
            cardsByOwner.computeIfAbsent(ownerOfCard(card, fallbackOwnerId), ignored -> new ArrayList<>()).add(card);
        }
        cardsByOwner.forEach((ownerId, cards) -> {
            List<Card> library = gameData.playerDecks.get(ownerId);
            int index = toBottom ? library.size() : Math.min(Math.max(0, position == null ? 0 : position), library.size());
            library.addAll(index, cards);
        });
        if (chooseOrder) queueMutationComponentOrders(gameData, target, fallbackOwnerId, Zone.LIBRARY, position);
    }

    /** Gives each owner the relative order of the components which reached their ordered zone. */
    private void queueMutationComponentOrders(GameData gameData, Permanent target, UUID fallbackOwnerId,
                                               Zone destination, Integer position) {
        Map<UUID, List<Card>> cardsByOwner = new java.util.LinkedHashMap<>();
        Map<UUID, List<Card>> zones = destination == Zone.GRAVEYARD ? gameData.playerGraveyards : gameData.playerDecks;
        for (Card card : target.cardsLeavingBattlefield()) {
            if (isToken(gameData, card)) continue;
            UUID ownerId = ownerOfCard(card, fallbackOwnerId);
            List<Card> zone = zones.getOrDefault(ownerId, List.of());
            if (zone.stream().anyMatch(present -> present.getId().equals(card.getId()))) {
                cardsByOwner.computeIfAbsent(ownerId, ignored -> new ArrayList<>()).add(card);
            }
        }
        cardsByOwner.forEach((ownerId, cards) -> {
            if (cards.size() < 2) return;
            String zoneName = destination == Zone.GRAVEYARD ? "graveyard" : "library";
            var choice = new com.github.laxika.magicalvibes.model.PendingInteraction.LibraryReorder(
                    ownerId, List.copyOf(cards), false, ownerId,
                    "Choose the relative order of the merged permanent's cards in your " + zoneName + ".",
                    0, null, false, false, destination, position);
            if (!gameData.interaction.isAwaitingInput()) {
                interactionHandlerRegistry.begin(gameData, choice);
            } else {
                gameData.pendingInteractions.addLast(choice);
            }
        });
    }

    private UUID ownerOfCard(Card card, UUID fallback) {
        return card.getOwnerId() == null ? fallback : card.getOwnerId();
    }

    /** Records a non-token card that currently has token status from a continuous effect. */
    private boolean markDynamicToken(GameData gameData, Permanent target) {
        if (target.isMergedByMutation() || target.getCard().isToken()
                || !gameQueryService.isToken(gameData, target)) {
            return false;
        }
        for (Card leaving : target.cardsLeavingBattlefield()) {
            gameData.dynamicTokenCardIds.add(leaving.getId());
        }
        return true;
    }

    private void clearDynamicToken(GameData gameData, Permanent target) {
        target.cardsLeavingBattlefield().forEach(card -> gameData.dynamicTokenCardIds.remove(card.getId()));
    }

    private boolean isToken(GameData gameData, Card card) {
        return card != null && (card.isToken() || gameData.dynamicTokenCardIds.contains(card.getId()));
    }
}
