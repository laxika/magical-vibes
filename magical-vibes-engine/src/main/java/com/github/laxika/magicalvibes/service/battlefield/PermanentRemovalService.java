package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import com.github.laxika.magicalvibes.model.effect.PersistReturnEffect;
import com.github.laxika.magicalvibes.model.effect.UndyingReturnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.CounterType;

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
    private final GameBroadcastService gameBroadcastService;
    private final ExileService exileService;

    public PermanentRemovalService(GraveyardService graveyardService,
                                   BattlefieldEntryService battlefieldEntryService,
                                   @Lazy TriggerCollectionService triggerCollectionService,
                                   DamagePreventionService damagePreventionService,
                                   AuraAttachmentService auraAttachmentService,
                                   GameQueryService gameQueryService,
                                   GameBroadcastService gameBroadcastService,
                                   ExileService exileService) {
        this.graveyardService = graveyardService;
        this.battlefieldEntryService = battlefieldEntryService;
        this.triggerCollectionService = triggerCollectionService;
        this.damagePreventionService = damagePreventionService;
        this.auraAttachmentService = auraAttachmentService;
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.exileService = exileService;
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
        // Replacement effect: exile instead of going to graveyard (CR 614.6)
        if (tryApplyExileReplacementEffect(gameData, target, true, "going to the graveyard")) {
            return true;
        }

        // Capture unattach-sacrifice info before removal
        UUID sacrificeOnUnattachCreatureId = getSacrificeOnUnattachCreatureId(target);

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        boolean wasArtifact = gameQueryService.isArtifact(target);
        boolean hadUndying = wasCreature && gameQueryService.hasKeyword(gameData, target, Keyword.UNDYING);
        boolean hadPersist = wasCreature && gameQueryService.hasKeyword(gameData, target, Keyword.PERSIST);
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();

        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        processGraveyardAndTriggers(gameData, target, wasCreature, wasArtifact, hadUndying, hadPersist, controllerId, ownerId);
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

        UUID sacrificeOnUnattachCreatureId = getSacrificeOnUnattachCreatureId(target);

        boolean wasCreature = gameQueryService.isCreature(gameData, target);
        boolean wasArtifact = gameQueryService.isArtifact(target);
        boolean hadUndying = wasCreature && gameQueryService.hasKeyword(gameData, target, Keyword.UNDYING);
        boolean hadPersist = wasCreature && gameQueryService.hasKeyword(gameData, target, Keyword.PERSIST);
        RemovedPermanentInfo info = processRemovalCleanup(gameData, target, controllerId);

        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, info.controllerId());
        processGraveyardAndTriggers(gameData, target, wasCreature, wasArtifact, hadUndying, hadPersist, info.controllerId(), info.ownerId());
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

        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        gameData.addCardToHand(ownerId, target.getOriginalCard());
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
        // Capture unattach-sacrifice info before removal
        UUID sacrificeOnUnattachCreatureId = getSacrificeOnUnattachCreatureId(target);

        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        exileService.exileCard(gameData, ownerId, target.getOriginalCard());
        handleSacrificeOnUnattach(gameData, target, sacrificeOnUnattachCreatureId);
        handleExileReturnOnLeave(gameData, target);
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
        // Replacement effect: exile instead of going to library (CR 614.6)
        if (tryApplyExileReplacementEffect(gameData, target, false, "going to the library")) {
            return true;
        }

        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        gameData.playerDecks.get(ownerId).add(0, target.getOriginalCard());
        handleExileReturnOnLeave(gameData, target);
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

        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        gameData.playerDecks.get(ownerId).add(target.getOriginalCard());
        handleExileReturnOnLeave(gameData, target);
        return true;
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

        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        List<Card> library = gameData.playerDecks.get(ownerId);
        int insertIndex = Math.min(position, library.size());
        library.add(insertIndex, target.getOriginalCard());
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

        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();
        triggerCollectionService.checkEnchantedPermanentLTBTriggers(gameData, target, controllerId);
        triggerCollectionService.checkSelfLeavesTriggered(gameData, target, controllerId);
        gameData.playerDecks.get(ownerId).add(target.getOriginalCard());
        LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        handleExileReturnOnLeave(gameData, target);
        return true;
    }

    /**
     * Removes all auras whose enchanted permanent is no longer on the battlefield.
     *
     * @param gameData the current game state
     */
    public void removeOrphanedAuras(GameData gameData) {
        var removals = auraAttachmentService.removeOrphanedAuras(gameData);
        for (var removal : removals) {
            triggerCollectionService.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gameData, removal.card(), removal.controllerId());
        }
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.isIndestructible(target.getCard()));
            log.info("Game {} - {} is indestructible, destroy prevented", gameData.id, target.getCard().getName());
            return false;
        }
        if (!cannotBeRegenerated && graveyardService.tryRegenerate(gameData, target)) {
            return false;
        }
        removePermanentToGraveyard(gameData, target);
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
                gameData.drainDelayedActions(DelayedPermanentAction.class, a -> a.kind() == kind);
        for (DelayedPermanentAction action : actions) {
            Permanent perm = gameQueryService.findPermanentById(gameData, action.permanentId());
            if (perm == null) {
                continue;
            }
            switch (kind.op()) {
                case EXILE -> removePermanentToExile(gameData, perm);
                case SACRIFICE -> removePermanentToGraveyard(gameData, perm);
                case RETURN_TO_HAND -> removePermanentToHand(gameData, perm);
                case DESTROY -> {
                    if (!tryDestroyPermanent(gameData, perm, action.cannotBeRegenerated())) {
                        continue;
                    }
                }
            }
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.builder().card(perm.getCard()).text(kind.logSuffix()).build());
            log.info("Game {} - {}{}", gameData.id, perm.getCard().getName(), kind.logSuffix());
            if (kind.op() != DelayedPermanentActionKind.Op.DESTROY) {
                removeOrphanedAuras(gameData);
            }
        }
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
            if (graveyard.removeIf(c -> c.getId().equals(cardId))) {
                Set<UUID> tracked = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.get(playerId);
                if (tracked != null) {
                    tracked.remove(cardId);
                }
                Set<UUID> allTracked = gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.get(playerId);
                if (allTracked != null) {
                    allTracked.remove(cardId);
                }
                graveyardService.notifyCardsLeftGraveyard(gameData, playerId);
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
        return redirectPlayerDamageToEnchantedCreature(gameData, playerId, damage, sourceName, false);
    }

    public int redirectPlayerDamageToEnchantedCreature(GameData gameData, UUID playerId, int damage, String sourceName, boolean isCombatDamage) {
        if (damage <= 0) return damage;
        Permanent target = gameQueryService.findEnchantedCreatureByAuraEffect(gameData, playerId, RedirectPlayerDamageToEnchantedCreatureEffect.class);
        if (target == null) {
            target = gameQueryService.findControlledPermanentWithStaticEffect(gameData, playerId, RedirectPlayerDamageToSelfEffect.class);
        }
        if (target == null) return damage;

        int effectiveDamage = damagePreventionService.applyCreaturePreventionShield(gameData, target, damage, isCombatDamage);
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.cardThen(target.getCard(), " absorbs " + effectiveDamage + " redirected " + sourceName + " damage."));

        if (effectiveDamage >= gameQueryService.getEffectiveToughness(gameData, target)) {
            if (tryDestroyPermanent(gameData, target)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.cardThen(target.getCard(), " is destroyed by redirected " + sourceName + " damage."));
            }
        }

        return 0;
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
        if (!target.isExileIfLeavesBattlefield() && !(checkExileInsteadOfDie && target.isExileInsteadOfDieThisTurn())) {
            return false;
        }
        boolean exiled = removePermanentToExile(gameData, target);
        if (exiled) {
            gameBroadcastService.logAndBroadcast(gameData,
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
            if (battlefield != null && battlefield.remove(target)) {
                return Optional.of(processRemovalCleanup(gameData, target, playerId));
            }
        }
        return Optional.empty();
    }

    /**
     * Performs all leaving-the-battlefield cleanup for a permanent that has already been removed
     * from the battlefield list. This is the single point where structural cleanup happens.
     */
    private RemovedPermanentInfo processRemovalCleanup(GameData gameData, Permanent target, UUID controllerId) {
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), controllerId);
        gameData.stolenCreatures.remove(target.getId());
        gameData.expireFloatingEffectsForDepartedSource(target.getId());
        gameData.expireControlEffectsForDepartedPermanent(target.getId());
        handleSourceLinkedAnimationCleanup(gameData, target);
        handlePreparedSpellCleanup(gameData, target);
        return new RemovedPermanentInfo(controllerId, ownerId);
    }

    /**
     * Sends a removed permanent's card to the graveyard and fires all death/graveyard triggers.
     */
    private void processGraveyardAndTriggers(GameData gameData, Permanent target,
                                              boolean wasCreature, boolean wasArtifact,
                                              boolean hadUndying, boolean hadPersist,
                                              UUID controllerId, UUID ownerId) {
        boolean wentToGraveyard = graveyardService.addCardToGraveyard(gameData, ownerId, target.getOriginalCard(), Zone.BATTLEFIELD);
        if (wentToGraveyard) {
            triggerCollectionService.collectDeathTrigger(gameData, target.getCard(), controllerId, wasCreature, target);
            // Any permanent an opponent controls is put into a graveyard (Prince of Thralls).
            triggerCollectionService.checkOpponentPermanentPutIntoGraveyardTriggers(
                    gameData, target.getOriginalCard(), controllerId, ownerId);
            if (wasCreature) {
                gameData.creatureDeathCountThisTurn.merge(controllerId, 1, Integer::sum);
                triggerCollectionService.checkAllyCreatureDeathTriggers(gameData, controllerId, target);
                triggerCollectionService.checkAnyCreatureDeathTriggers(gameData, controllerId, target);
                triggerCollectionService.checkAllyNontokenCreatureDeathTriggers(gameData, controllerId, target.getCard());
                triggerCollectionService.checkAnyNontokenCreatureDeathTriggers(gameData, target.getCard());
                triggerCollectionService.checkOpponentCreatureDeathTriggers(gameData, controllerId, target);
                triggerCollectionService.checkEquippedCreatureDeathTriggers(gameData, target.getId(), controllerId, target.getCard());
                triggerCollectionService.triggerDelayedPoisonOnDeath(gameData, target.getCard().getId(), controllerId);
                triggerCollectionService.triggerDelayedReturnOnDeath(gameData, target.getCard().getId(), target.getOriginalCard(), ownerId);
                triggerCollectionService.triggerDelayedCreateTokenOnDeath(gameData, target.getCard().getId());
                collectUndyingTrigger(gameData, target, ownerId, hadUndying);
                collectPersistTrigger(gameData, target, ownerId, hadPersist);
            }
            if (wasArtifact) {
                triggerCollectionService.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gameData, ownerId, controllerId);
            }
            if (target.getCard().hasType(CardType.LAND)) {
                triggerCollectionService.checkLandPutIntoGraveyardByOpponentTriggers(
                        gameData, target.getOriginalCard(), ownerId, gameData.currentlyResolvingControllerId);
                triggerCollectionService.checkAnyLandPutIntoGraveyardFromBattlefieldTriggers(gameData, ownerId, controllerId);
            }
            triggerCollectionService.checkEnchantedPermanentDeathTriggers(gameData, target.getId(), controllerId,
                    target.getCard().getId(), target.getEffectiveToughness());
            // Check if the dying permanent was an Aura or Equipment (Tiana, Ship's Caretaker)
            if (target.getCard().isAura() || target.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                triggerCollectionService.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gameData, target.getCard(), controllerId);
            }
        }
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(dyingCard, "'s undying ability triggers."));
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(dyingCard, "'s persist ability triggers."));
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
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

                    gameBroadcastService.logAndBroadcast(gameData,
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
            returnPendingExiledCard(gameData, pending);
        }
    }

    private void returnPendingExiledCard(GameData gameData, PendingExileReturn pending) {
        Card exiledCard = pending.card();
        UUID ownerId = pending.controllerId();

        // Remove card from exile zone
        if (gameData.removeFromExile(exiledCard.getId())) {
            String playerName = gameData.playerIdToName.get(ownerId);

            if (pending.returnToHand()) {
                // Return to owner's hand (e.g. Kitesail Freebooter — exiled from hand)
                gameData.playerHands.get(ownerId).add(exiledCard);
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.cardThen(exiledCard, " returns to " + playerName + "'s hand."));
                log.info("Game {} - {} returns to hand from exile (source left battlefield)", gameData.id, exiledCard.getName());
            } else {
                // Return as a new permanent on the battlefield, tapped iff requested (e.g. Realm Razer)
                Permanent perm = new Permanent(exiledCard);
                if (pending.returnTapped()) {
                    perm.tap();
                }
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, perm);
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.cardThen(exiledCard, " returns to the battlefield under " + playerName + "'s control."));
                log.info("Game {} - {} returns from exile (source left battlefield)", gameData.id, exiledCard.getName());
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, exiledCard, null, false);
            }
        } else {
            log.info("Game {} - Exiled card {} no longer in exile zone, return skipped", gameData.id, exiledCard.getName());
        }
    }
}
