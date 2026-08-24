package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AuraCopyService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesDamagedBySourceInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCreaturesInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentCreatureCardExileReplacement;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureLibraryReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PutOnTopOfLibraryInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import com.github.laxika.magicalvibes.model.effect.PersistReturnEffect;
import com.github.laxika.magicalvibes.model.effect.UndyingReturnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.effect.normalfx.UnattachTriggerSupport;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;

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
    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final UntapLockReleaseService untapLockReleaseService;
    private final AuraCopyService auraCopyService;
    private final CreatureControlService creatureControlService;
    private final UnattachTriggerSupport unattachTriggerSupport;
    private final PlayerInputService playerInputService;

    public PermanentRemovalService(GraveyardService graveyardService,
                                   BattlefieldEntryService battlefieldEntryService,
                                   @Lazy TriggerCollectionService triggerCollectionService,
                                   DamagePreventionService damagePreventionService,
                                   AuraAttachmentService auraAttachmentService,
                                   GameQueryService gameQueryService,
                                   GameLogService gameLogService,
                                   ExileService exileService,
                                   UntapLockReleaseService untapLockReleaseService,
                                   @Lazy AuraCopyService auraCopyService,
                                   @Lazy CreatureControlService creatureControlService,
                                   UnattachTriggerSupport unattachTriggerSupport,
                                   @Lazy PlayerInputService playerInputService) {
        this.graveyardService = graveyardService;
        this.battlefieldEntryService = battlefieldEntryService;
        this.triggerCollectionService = triggerCollectionService;
        this.damagePreventionService = damagePreventionService;
        this.auraAttachmentService = auraAttachmentService;
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.exileService = exileService;
        this.untapLockReleaseService = untapLockReleaseService;
        this.auraCopyService = auraCopyService;
        this.creatureControlService = creatureControlService;
        this.unattachTriggerSupport = unattachTriggerSupport;
        this.playerInputService = playerInputService;
    }

    public void setTriggerCollectionService(TriggerCollectionService triggerCollectionService) {
        this.triggerCollectionService = triggerCollectionService;
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

    /**
     * Moves a permanent to its graveyard as the result of destruction after the caller has
     * applied destruction-specific replacement checks such as indestructible and regeneration.
     */
    public boolean destroyPermanentToGraveyard(GameData gameData, Permanent target) {
        return removePermanentToGraveyard(gameData, target, true);
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
        // Replacement effect: exile instead of going to graveyard (CR 614.6)
        if (tryApplyExileReplacementEffect(gameData, target, true, "going to the graveyard")) {
            return true;
        }

        if (!ignoreMayLibraryReplacement && offerMayLibraryReplacement(gameData, target)) {
            return false;
        }

        // Capture unattach-sacrifice info before removal
        UUID sacrificeOnUnattachCreatureId = getSacrificeOnUnattachCreatureId(target);

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        int dyingPowerAtDeath = wasCreature
                ? Math.max(0, gameQueryService.getEffectivePower(gameData, target))
                : 0;
        List<CardEffect> grantedDeathEffects = wasCreature
                ? triggerCollectionService.grantedTriggeredEffects(gameData, target, EffectSlot.ON_DEATH)
                : List.of();
        boolean wasArtifact = gameQueryService.isArtifact(target);
        boolean wasEnchantment = gameQueryService.isEnchantment(gameData, target);
        Set<CardSubtype> creatureSubtypesAtDeath = wasCreature
                ? gameQueryService.effectiveCreatureSubtypes(gameData, target)
                : Set.of();
        boolean hadUndying = wasCreature && gameQueryService.hasKeyword(gameData, target, Keyword.UNDYING);
        boolean hadPersist = wasCreature && gameQueryService.hasKeyword(gameData, target, Keyword.PERSIST);
        boolean selfGraveyardTriggerSuppressed = selfGraveyardTriggerSuppressed(gameData, target);
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();

        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.GRAVEYARD);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAnotherNontokenArtifactPutIntoGraveyardOrExileFromBattlefieldTriggers(
                gameData, target, controllerId, Zone.GRAVEYARD);
        processGraveyardAndTriggers(gameData, target, wasCreature, wasArtifact, wasEnchantment,
                creatureSubtypesAtDeath, hadUndying, hadPersist, controllerId, ownerId,
                destroyedBySpellOrAbility, grantedDeathEffects, dyingPowerAtDeath,
                selfGraveyardTriggerSuppressed);
        handleSacrificeOnUnattach(gameData, target, sacrificeOnUnattachCreatureId);
        handleExileReturnOnLeave(gameData, target);
        return true;
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
        // Replacement effect: exile instead of going to graveyard (CR 614.6)
        if (tryApplyExileReplacementEffect(gameData, target, true, "going to the graveyard")) {
            return;
        }

        unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, target, target.getAttachedTo(), controllerId);
        UUID sacrificeOnUnattachCreatureId = getSacrificeOnUnattachCreatureId(target);

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        int dyingPowerAtDeath = wasCreature ? Math.max(0, target.getEffectivePower()) : 0;
        List<CardEffect> grantedDeathEffects = wasCreature
                ? triggerCollectionService.grantedTriggeredEffects(gameData, target, EffectSlot.ON_DEATH)
                : List.of();
        boolean wasArtifact = gameQueryService.isArtifact(target);
        boolean wasEnchantment = gameQueryService.isEnchantment(gameData, target);
        Set<CardSubtype> creatureSubtypesAtDeath = wasCreature
                ? gameQueryService.effectiveCreatureSubtypes(gameData, target)
                : Set.of();
        boolean hadUndying = wasCreature && gameQueryService.hasKeyword(gameData, target, Keyword.UNDYING);
        boolean hadPersist = wasCreature && gameQueryService.hasKeyword(gameData, target, Keyword.PERSIST);
        boolean selfGraveyardTriggerSuppressed = selfGraveyardTriggerSuppressed(gameData, target);
        RemovedPermanentInfo info = processRemovalCleanup(gameData, target, controllerId);

        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.GRAVEYARD);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, info.controllerId());
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, info.controllerId());
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, info.controllerId());
        triggerCollectionService.checkAnotherNontokenArtifactPutIntoGraveyardOrExileFromBattlefieldTriggers(
                gameData, target, info.controllerId(), Zone.GRAVEYARD);
        processGraveyardAndTriggers(gameData, target, wasCreature, wasArtifact, wasEnchantment,
                creatureSubtypesAtDeath, hadUndying, hadPersist, info.controllerId(), info.ownerId(), false,
                grantedDeathEffects, dyingPowerAtDeath, selfGraveyardTriggerSuppressed);
        handleSacrificeOnUnattach(gameData, target, sacrificeOnUnattachCreatureId);
        handleExileReturnOnLeave(gameData, target);
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
        // Replacement effect: exile instead of going to hand (CR 614.6)
        if (tryApplyExileReplacementEffect(gameData, target, false, "returning to hand")) {
            return true;
        }

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.HAND);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        for (Card leaving : target.cardsLeavingBattlefield()) {
            gameData.addCardToHand(ownerId, leaving);
        }
        gameData.playersWhoReceivedPermanentFromBattlefieldToHandThisTurn.add(ownerId);
        forgetDamageDealtToDepartedPermanent(gameData, target);
        handleExileReturnOnLeave(gameData, target);
        triggerCollectionService.checkPermanentReturnedToHandTriggers(gameData, ownerId);
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

    /**
     * Removes a permanent to exile and records its cards as exiled with {@code sourcePermanentId}
     * when one is supplied.
     */
    public boolean removePermanentToExile(GameData gameData, Permanent target, UUID sourcePermanentId) {
        // Capture unattach-sacrifice info before removal
        UUID sacrificeOnUnattachCreatureId = getSacrificeOnUnattachCreatureId(target);

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.EXILE);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId, Zone.EXILE);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        triggerCollectionService.checkAnotherNontokenArtifactPutIntoGraveyardOrExileFromBattlefieldTriggers(
                gameData, target, controllerId, Zone.EXILE);
        for (Card leaving : target.cardsLeavingBattlefield()) {
            if (sourcePermanentId == null) {
                exileService.exileCard(gameData, ownerId, leaving);
            } else {
                exileService.exileCard(gameData, ownerId, leaving, sourcePermanentId);
            }
        }
        graveyardService.notifyCardsExiledFromBattlefield(gameData, target.cardsLeavingBattlefield().size());
        forgetDamageDealtToDepartedPermanent(gameData, target);
        handleSacrificeOnUnattach(gameData, target, sacrificeOnUnattachCreatureId);
        handleExileReturnOnLeave(gameData, target);
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
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.LIBRARY);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        for (Card leaving : target.cardsLeavingBattlefield()) {
            gameData.playerDecks.get(ownerId).add(0, leaving);
        }
        forgetDamageDealtToDepartedPermanent(gameData, target);
        handleExileReturnOnLeave(gameData, target);
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
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.LIBRARY);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        for (Card leaving : target.cardsLeavingBattlefield()) {
            gameData.playerDecks.get(ownerId).add(leaving);
        }
        forgetDamageDealtToDepartedPermanent(gameData, target);
        handleExileReturnOnLeave(gameData, target);
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
        for (Permanent perm : permanents) {
            if (removePermanentToLibraryBottom(gameData, perm)) {
                moved.add(perm);
            }
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
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.LIBRARY);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        List<Card> library = gameData.playerDecks.get(ownerId);
        int insertIndex = Math.min(position, library.size());
        for (Card leaving : target.cardsLeavingBattlefield()) {
            library.add(Math.min(insertIndex, library.size()), leaving);
            insertIndex++;
        }
        handleExileReturnOnLeave(gameData, target);
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
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId, Zone.LIBRARY);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        triggerCollectionService.processDelayedSacrificeSourceWhenTargetLeaves(gameData, target);
        triggerCollectionService.processDelayedSacrificeTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.processDelayedDestroyTargetWhenSourceLeaves(gameData, target);
        triggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature);
        triggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers(gameData, target, wasCreature, controllerId);
        triggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers(gameData, target, controllerId);
        for (Card leaving : target.cardsLeavingBattlefield()) {
            gameData.playerDecks.get(ownerId).add(leaving);
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        handleExileReturnOnLeave(gameData, target);
        return true;
    }

    /**
     * Finds a card that has already left the battlefield and shuffles it into its owner's library.
     * This is used by triggered abilities whose source may leave before resolution.
     */
    public boolean shuffleCardIntoOwnerLibrary(GameData gameData, Card card, UUID fallbackOwnerId) {
        if (card == null || card.isToken()) {
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
        var result = auraAttachmentService.removeOrphanedAuras(gameData);
        for (var removal : result.removals()) {
            triggerCollectionService.collectDeathTrigger(gameData, removal.card(), removal.controllerId(), false);
            triggerCollectionService.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gameData, removal.card(), removal.controllerId());
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
            triggerCollectionService.collectDeathTrigger(gameData, removal.card(), removal.controllerId(), false);
            triggerCollectionService.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gameData, removal.card(), removal.controllerId());
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
        if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
            gameLogService.append(gameData, GameLog.isIndestructible(target.getCard()));
            log.info("Game {} - {} is indestructible, destroy prevented", gameData.id, target.getCard().getName());
            return false;
        }
        if (graveyardService.tryReplaceDestruction(gameData, target, !cannotBeRegenerated)) {
            return false;
        }
        destroyPermanentToGraveyard(gameData, target);
        removeOrphanedAuras(gameData);
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
                                && (a.controllerId() == null || a.controllerId().equals(gameData.activePlayerId)));
        for (DelayedPermanentAction action : actions) {
            Permanent perm = gameQueryService.findPermanentById(gameData, action.permanentId());
            if (perm == null) {
                continue;
            }
            switch (kind.op()) {
                case EXILE -> removePermanentToExile(gameData, perm);
                case SACRIFICE -> {
                    if (gameQueryService.cantBeSacrificed(gameData, perm)) {
                        continue;
                    }
                    UUID sacrificeControllerId = gameQueryService.findPermanentController(gameData, perm.getId());
                    boolean sacrificed = removePermanentToGraveyard(gameData, perm);
                    if (sacrificed && sacrificeControllerId != null) {
                        triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                                gameData, sacrificeControllerId, perm.getCard());
                    }
                    if (sacrificed) {
                        returnExiledCardToBattlefield(gameData, action.returnExiledCardId());
                    }
                }
                case RETURN_TO_HAND -> removePermanentToHand(gameData, perm);
                case PUT_ON_TOP_OF_LIBRARY -> removePermanentToLibraryTop(gameData, perm);
                case DESTROY -> {
                    if (!tryDestroyPermanent(gameData, perm, action.cannotBeRegenerated())) {
                        continue;
                    }
                }
            }
            gameLogService.append(gameData,
                    GameLog.builder().card(perm.getCard()).text(kind.logSuffix()).build());
            log.info("Game {} - {}{}", gameData.id, perm.getCard().getName(), kind.logSuffix());
            if (kind.op() != DelayedPermanentActionKind.Op.DESTROY) {
                removeOrphanedAuras(gameData);
            }
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
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, new Permanent(card));
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
                graveyardService.notifyCardLeftGraveyard(gameData, playerId, leaving);
                return;
            }
        }
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
                graveyardService.notifyCardsExiledFromGraveyard(gameData, playerId, leaving);
                return;
            }
        }
    }

    /**
     * Checks if the player has an aura with {@link RedirectPlayerDamageToEnchantedCreatureEffect}
     * (e.g. Pariah) and redirects incoming damage to the enchanted creature. Destroys the creature
     * if the redirected damage meets or exceeds its toughness.
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
        if (damage <= 0) return damage;
        Permanent target = gameQueryService.findEnchantedCreatureByAuraEffect(gameData, playerId, RedirectPlayerDamageToEnchantedCreatureEffect.class);
        boolean sourceRestrictedRedirect = false;
        if (target == null) {
            target = findControlledPermanentWithDamageRedirect(gameData, playerId, sourcePermanentId);
            sourceRestrictedRedirect = target != null && target.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof RedirectPlayerDamageToSelfEffect redirect
                            && redirect.onlyFromUnblockedCreatures());
        }
        if (target == null) return damage;

        int effectiveDamage = damagePreventionService.applyCreaturePreventionShield(gameData, target, damage, isCombatDamage);
        gameLogService.append(gameData,
                GameLog.cardThen(target.getCard(), " absorbs " + effectiveDamage + " redirected " + sourceName + " damage."));

        if (sourceRestrictedRedirect) {
            if (effectiveDamage > 0) {
                target.addMarkedDamage(sourcePermanentId, effectiveDamage);
                gameData.recordDamageToPermanent(target.getId(), effectiveDamage);
                gameData.recordDamageDealtBySource(sourcePermanentId, effectiveDamage);
                if (sourcePermanentId != null) {
                    gameData.recordDamageRecipientBySource(sourcePermanentId, target.getId());
                    Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                    if (source != null) {
                        if (gameQueryService.hasKeyword(gameData, source, Keyword.DEATHTOUCH)) {
                            target.setDamagedByDeathtouch(true);
                        }
                        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
                        graveyardService.recordCreatureDamagedByPermanent(gameData, sourcePermanentId, target, effectiveDamage);
                        triggerCollectionService.checkDealtDamageToCreatureTriggers(
                                gameData, target, effectiveDamage, sourceControllerId);
                    }
                }
            }
            return 0;
        }

        if (effectiveDamage >= gameQueryService.getEffectiveToughness(gameData, target)) {
            if (tryDestroyPermanent(gameData, target)) {
                gameLogService.append(gameData,
                        GameLog.cardThen(target.getCard(), " is destroyed by redirected " + sourceName + " damage."));
            }
        }

        return 0;
    }

    private Permanent findControlledPermanentWithDamageRedirect(GameData gameData, UUID playerId,
                                                                  UUID sourcePermanentId) {
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (!(effect instanceof RedirectPlayerDamageToSelfEffect redirect)) continue;
                if (redirect.onlyFromUnblockedCreatures()) {
                    if (permanent.isTapped() || !gameQueryService.isCreature(gameData, permanent)
                            || !damagePreventionService.isUnblockedCreatureSource(gameData, sourcePermanentId)) {
                        continue;
                    }
                }
                return permanent;
            }
        }
        return null;
    }

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
                && GraveyardService.hasExilePermanentsInsteadOfGraveyardReplacementEffect(target.getCard());
        if (!target.isExileIfLeavesBattlefield()
                && !target.isExileIfLeavesBattlefieldUntilEndOfTurn()
                && !(checkExileInsteadOfDie && target.isExileIfDying())
                && !(checkExileInsteadOfDie && target.isExileInsteadOfDieThisTurn())
                && !(checkExileInsteadOfDie && target.getCounterCount(CounterType.FINALITY) > 0)
                && !permanentGraveyardReplacement) {
            return false;
        }
        boolean exiled = removePermanentToExile(gameData, target);
        if (exiled) {
            gameLogService.append(gameData,
                    GameLog.cardThen(target.getCard(), " is exiled instead of " + destinationDescription + "."));
            removeOrphanedAuras(gameData);
        }
        return exiled;
    }

    private record RemovedPermanentInfo(UUID controllerId, UUID ownerId) {}

    /**
     * Finds and removes the given permanent from whatever battlefield it's on, cleans up
     * stolen-creature and permanent-exiled-cards tracking, and returns controller/owner info.
     */
    private Optional<RemovedPermanentInfo> removeFromBattlefield(GameData gameData, Permanent target) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(target)) {
                unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, target, target.getAttachedTo(), playerId);
                battlefield.remove(target);
                preserveBlockedStatusWhenBlockerLeaves(gameData, target);
                return Optional.of(processRemovalCleanup(gameData, target, playerId));
            }
        }
        return Optional.empty();
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

    private void forgetDamageDealtToDepartedPermanent(GameData gameData, Permanent departed) {
        UUID cardId = departed.getCard().getId();
        for (Set<UUID> damagedCardIds : gameData.creatureCardsDamagedThisTurnBySourcePermanent.values()) {
            damagedCardIds.remove(cardId);
        }
    }

    /**
     * Performs all leaving-the-battlefield cleanup for a permanent that has already been removed
     * from the battlefield list. This is the single point where structural cleanup happens.
     */
    private RemovedPermanentInfo processRemovalCleanup(GameData gameData, Permanent target, UUID controllerId) {
        gameData.playersWhosePermanentsLeftBattlefieldThisTurn.add(controllerId);
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), controllerId);
        gameData.stolenCreatures.remove(target.getId());
        // A departing Aura ends the layer-1 copy it granted (Metamorphic Alteration): its
        // WHILE_ATTACHED floating effect expires here and drives the enchanted creature's revert.
        auraCopyService.revertExpiredCopies(gameData,
                gameData.expireFloatingEffectsForDepartedSource(target.getId()));
        gameData.expireControlEffectsForDepartedPermanent(target.getId());
        creatureControlService.reconcileControl(gameData);
        untapLockReleaseService.releaseUntapLocks(gameData, target);
        handleSourceLinkedAnimationCleanup(gameData, target);
        handlePreparedSpellCleanup(gameData, target);
        clearSoulbondPairing(gameData, target);
        return new RemovedPermanentInfo(controllerId, ownerId);
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
                        .filter(candidate -> !candidate.nontokenOnly() || !dyingCard.isToken())
                        .findFirst().orElse(null);
                if (effect != null) {
                    return new OpponentDyingCreatureExileReplacement(
                            effect, permanent.getCard(), playerId, permanent.getId());
                }
            }
        }
        return null;
    }

    private record OpponentDyingCreatureExileReplacement(
            ExileOpponentCreaturesInsteadOfDyingEffect effect,
            Card sourceCard,
            UUID controllerId,
            UUID sourcePermanentId) {}

    /**
     * True when a player other than {@code ownerId} controls a permanent with an opponent-owned
     * creature-card graveyard replacement, and the dying card is not a token.
     */
    private boolean opponentExilesOwnedNontokenCreature(GameData gameData, UUID ownerId, Card card) {
        if (card.isToken()) {
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
                if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(OpponentCreatureCardExileReplacement.class::isInstance)) {
                    return true;
                }
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
                                              boolean wasCreature, boolean wasArtifact,
                                              boolean wasEnchantment,
                                              Set<CardSubtype> creatureSubtypesAtDeath,
                                              boolean hadUndying, boolean hadPersist,
                                              UUID controllerId, UUID ownerId,
                                              boolean destroyedBySpellOrAbility,
                                              List<CardEffect> grantedDeathEffects,
                                              int dyingPowerAtDeath,
                                              boolean selfGraveyardTriggerSuppressed) {
        boolean wentToGraveyard = false;
        int exiledFromBattlefield = 0;
        // Disturb back-face (etc.): exile-instead is printed on the current face; the physical
        // card that leaves is still originalCard / meld components.
        OpponentDyingCreatureExileReplacement opponentExileReplacement = wasCreature
                ? opponentDyingCreatureExileReplacement(gameData, controllerId, target.getCard())
                : null;
        boolean exileInstead = GraveyardService.hasExileInsteadOfGraveyardReplacementEffect(target.getCard())
                || opponentExileReplacement != null
                || (wasCreature && opponentExilesOwnedNontokenCreature(gameData, ownerId, target.getCard()))
                || (wasCreature && damagerExilesDyingCreature(gameData, target))
                || (wasCreature && !gameData.playersExilingCreaturesInsteadOfDyingThisTurn.isEmpty());
        for (Card leaving : target.cardsLeavingBattlefield()) {
            if (exileInstead) {
                if (opponentExileReplacement != null && opponentExileReplacement.effect().trackWithSource()) {
                    exileService.exileCard(gameData, ownerId, leaving,
                            opponentExileReplacement.sourcePermanentId());
                } else {
                    exileService.exileCard(gameData, ownerId, leaving);
                }
                if (opponentExileReplacement != null && opponentExileReplacement.effect().addIceCounter()) {
                    gameData.exiledCardsWithIceCounters.add(leaving.getId());
                }
                exiledFromBattlefield++;
                gameLogService.append(gameData,
                        GameLog.cardThen(leaving, " is exiled instead of being put into a graveyard."));
            } else {
                boolean enteredGraveyard = graveyardService.addCardToGraveyard(
                        gameData, ownerId, leaving, Zone.BATTLEFIELD, controllerId, target,
                        selfGraveyardTriggerSuppressed);
                if (enteredGraveyard) {
                    wentToGraveyard = true;
                } else if (gameData.findExiledCard(leaving.getId()) != null) {
                    exiledFromBattlefield++;
                }
            }
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
            }
        }
        graveyardService.notifyCardsExiledFromBattlefield(gameData, exiledFromBattlefield);
        if (wentToGraveyard) {
            if (target.getCounterCount(CounterType.OIL) > 0) {
                gameData.recordPermanentWithOilCounterPutIntoGraveyard();
            }
            triggerCollectionService.collectDeathTrigger(gameData, target.getCard(), controllerId, wasCreature, target,
                    grantedDeathEffects);
            // Any permanent an opponent controls is put into a graveyard (Prince of Thralls).
            triggerCollectionService.checkOpponentPermanentPutIntoGraveyardTriggers(
                    gameData, target.getOriginalCard(), controllerId, ownerId);
            // Any permanent owned by another player is put into a graveyard (Kothophed, Soul Hoarder).
            triggerCollectionService.checkOtherPlayerOwnedPermanentPutIntoGraveyardTriggers(
                    gameData, target.getOriginalCard(), ownerId);
            // "Whenever a creature or planeswalker you control dies" — fires once even when the
            // dying permanent is both (Ajani's Last Stand).
            if (wasCreature || target.getCard().hasType(CardType.PLANESWALKER)) {
                triggerCollectionService.checkAllyCreatureOrPlaneswalkerDeathTriggers(
                        gameData, controllerId, target, wasCreature);
            }
            // Any permanent at all is put into a graveyard (Yomiji, Who Bars the Way).
            triggerCollectionService.checkAnyPermanentPutIntoGraveyardTriggers(
                    gameData, target.getOriginalCard(), controllerId, ownerId);
            if (wasCreature) {
                gameData.creatureDeathCountThisTurn.merge(controllerId, 1, Integer::sum);
                if (!target.getCard().isToken()) {
                    gameData.nontokenCreatureDeathCountThisTurn.merge(controllerId, 1, Integer::sum);
                }
                Map<CardSubtype, Integer> subtypeCounts = gameData.creatureSubtypeDeathCountThisTurn
                        .computeIfAbsent(controllerId, ignored -> new java.util.concurrent.ConcurrentHashMap<>());
                for (CardSubtype subtype : creatureSubtypesAtDeath) {
                    subtypeCounts.merge(subtype, 1, Integer::sum);
                }
                triggerCollectionService.checkCreaturePutIntoOwnersGraveyardFromBattlefieldTriggers(
                        gameData, target.getOriginalCard(), ownerId, controllerId);
                triggerCollectionService.checkAllyCreatureDeathTriggers(
                        gameData, controllerId, target, dyingPowerAtDeath);
                triggerCollectionService.checkGraveyardAllyCreatureDeathTriggers(gameData, controllerId, target);
                triggerCollectionService.checkAnyCreatureDeathTriggers(gameData, controllerId, target);
                triggerCollectionService.checkAllyNontokenCreatureDeathTriggers(gameData, controllerId, target.getCard());
                triggerCollectionService.checkAnyNontokenCreatureDeathTriggers(gameData, target.getCard());
                triggerCollectionService.checkOpponentCreatureDeathTriggers(gameData, controllerId, target);
                triggerCollectionService.checkEquippedCreatureDeathTriggers(
                        gameData, target.getId(), controllerId, target.getCard(), dyingPowerAtDeath);
                triggerCollectionService.triggerDelayedPoisonOnDeath(gameData, target.getCard().getId(), controllerId);
                collectUndyingTrigger(gameData, target, ownerId, hadUndying);
                collectPersistTrigger(gameData, target, ownerId, hadPersist);
            }
            triggerCollectionService.triggerDelayedEffectOnDeath(
                    gameData, target.getCard().getId(), controllerId, target.getEffectivePower(),
                    target.getCard().getManaValue());
            triggerCollectionService.triggerDelayedReturnOnDeath(
                    gameData, target.getCard().getId(), target.getOriginalCard(), ownerId);
            if (wasArtifact) {
                triggerCollectionService.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(
                        gameData, ownerId, controllerId, target.getCard().getManaValue());
            }
            if (wasEnchantment) {
                triggerCollectionService.checkAnyEnchantmentPutIntoGraveyardFromBattlefieldTriggers(gameData, ownerId, controllerId);
            }
            if (target.getCard().hasType(CardType.LAND)) {
                triggerCollectionService.checkLandPutIntoGraveyardByOpponentTriggers(
                        gameData, target.getOriginalCard(), ownerId, gameData.currentlyResolvingControllerId);
                triggerCollectionService.checkAnyLandPutIntoGraveyardFromBattlefieldTriggers(gameData, ownerId, controllerId);
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
                    target.getCard().getId(), target.getEffectivePower(), target.getEffectiveToughness(), wasCreature);
            // Check if the dying permanent was an Aura or Equipment (Tiana, Ship's Caretaker)
            if (target.getCard().isAura() || target.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                triggerCollectionService.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gameData, target.getCard(), controllerId);
            }
        }
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
        if (target.getCard().getEffects(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD).isEmpty()) {
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
    private void collectUndyingTrigger(GameData gameData, Permanent dyingPermanent, UUID ownerId, boolean hadUndying) {
        if (!hadUndying) return;
        if (dyingPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0) return;

        Card dyingCard = dyingPermanent.getOriginalCard();
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                dyingCard,
                ownerId,
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
    private void collectPersistTrigger(GameData gameData, Permanent dyingPermanent, UUID ownerId, boolean hadPersist) {
        if (!hadPersist) return;
        if (dyingPermanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0) return;

        Card dyingCard = dyingPermanent.getOriginalCard();
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                dyingCard,
                ownerId,
                dyingCard.getName() + "'s persist ability",
                new ArrayList<>(List.of(new PersistReturnEffect()))
        ));
        gameLogService.append(gameData, GameLog.cardThen(dyingCard, "'s persist ability triggers."));
        log.info("Game {} - {} persist triggers", gameData.id, dyingCard.getName());
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
        removePermanentToGraveyard(gameData, creature);
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
    private void handleExileReturnOnLeave(GameData gameData, Permanent removedPermanent) {
        List<PendingExileReturn> pendingReturns = gameData.exileReturnOnPermanentLeave.remove(removedPermanent.getId());
        if (pendingReturns == null) return;

        for (PendingExileReturn pending : pendingReturns) {
            returnPendingExiledCard(gameData, removedPermanent.getId(), pending);
        }
    }

    private void returnPendingExiledCard(GameData gameData, UUID sourcePermanentId,
                                         PendingExileReturn pending) {
        Card exiledCard = pending.card();
        UUID ownerId = pending.controllerId();

        ExiledCardEntry currentExileEntry = gameData.findExiledCard(exiledCard.getId());
        if (pending.returnToGraveyard()
                && (currentExileEntry == null
                || !sourcePermanentId.equals(currentExileEntry.sourcePermanentId()))) {
            log.info("Game {} - Idol-linked card {} no longer exiled with its source, return skipped",
                    gameData.id, exiledCard.getName());
            return;
        }

        // Remove card from exile zone
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
                gameData.playerHands.get(ownerId).add(exiledCard);
                gameLogService.append(gameData,
                        GameLog.cardThen(exiledCard, " returns to " + playerName + "'s hand."));
                log.info("Game {} - {} returns to hand from exile (source left battlefield)", gameData.id, exiledCard.getName());
            } else {
                // Return as a new permanent on the battlefield, tapped iff requested (e.g. Realm Razer)
                Permanent perm = new Permanent(exiledCard);
                if (pending.returnTapped()) {
                    perm.tap();
                }
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, perm);
                gameLogService.append(gameData,
                        GameLog.cardThen(exiledCard, " returns to the battlefield under " + playerName + "'s control."));
                log.info("Game {} - {} returns from exile (source left battlefield)", gameData.id, exiledCard.getName());
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, exiledCard, null, false);
            }
        } else {
            log.info("Game {} - Exiled card {} no longer in exile zone, return skipped", gameData.id, exiledCard.getName());
        }
    }
}
