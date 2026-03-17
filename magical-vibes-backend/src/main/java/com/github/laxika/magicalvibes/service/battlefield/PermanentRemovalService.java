package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.DeathTriggerService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
@RequiredArgsConstructor
public class PermanentRemovalService {

    private final GraveyardService graveyardService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final DeathTriggerService deathTriggerService;
    private final DamagePreventionService damagePreventionService;
    private final AuraAttachmentService auraAttachmentService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final ExileService exileService;

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
        Optional<RemovedPermanentInfo> removed = removeFromBattlefield(gameData, target);
        if (removed.isEmpty()) {
            return false;
        }
        UUID controllerId = removed.get().controllerId();
        UUID ownerId = removed.get().ownerId();

        processGraveyardAndTriggers(gameData, target, wasCreature, wasArtifact, controllerId, ownerId);
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
        RemovedPermanentInfo info = processRemovalCleanup(gameData, target, controllerId);

        processGraveyardAndTriggers(gameData, target, wasCreature, wasArtifact, info.controllerId(), info.ownerId());
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
        UUID ownerId = removed.get().ownerId();
        gameData.addCardToHand(ownerId, target.getOriginalCard());
        handleExileReturnOnLeave(gameData, target);
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
        UUID ownerId = removed.get().ownerId();
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
        UUID ownerId = removed.get().ownerId();
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
        UUID ownerId = removed.get().ownerId();
        gameData.playerDecks.get(ownerId).add(target.getOriginalCard());
        handleExileReturnOnLeave(gameData, target);
        return true;
    }

    /**
     * Removes all auras whose enchanted permanent is no longer on the battlefield.
     *
     * @param gameData the current game state
     */
    public void removeOrphanedAuras(GameData gameData) {
        auraAttachmentService.removeOrphanedAuras(gameData);
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
            String logEntry = target.getCard().getName() + " is indestructible.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
        if (target == null) return damage;

        int effectiveDamage = damagePreventionService.applyCreaturePreventionShield(gameData, target, damage, isCombatDamage);
        String logEntry = target.getCard().getName() + " absorbs " + effectiveDamage + " redirected " + sourceName + " damage.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        if (effectiveDamage >= gameQueryService.getEffectiveToughness(gameData, target)) {
            if (tryDestroyPermanent(gameData, target)) {
                String deathLog = target.getCard().getName() + " is destroyed by redirected " + sourceName + " damage.";
                gameBroadcastService.logAndBroadcast(gameData, deathLog);
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
            String logEntry = target.getCard().getName() + " is exiled instead of " + destinationDescription + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
        handleSourceLinkedAnimationCleanup(gameData, target);
        return new RemovedPermanentInfo(controllerId, ownerId);
    }

    /**
     * Sends a removed permanent's card to the graveyard and fires all death/graveyard triggers.
     */
    private void processGraveyardAndTriggers(GameData gameData, Permanent target,
                                              boolean wasCreature, boolean wasArtifact,
                                              UUID controllerId, UUID ownerId) {
        boolean wentToGraveyard = graveyardService.addCardToGraveyard(gameData, ownerId, target.getOriginalCard(), Zone.BATTLEFIELD);
        if (wentToGraveyard) {
            deathTriggerService.collectDeathTrigger(gameData, target.getCard(), controllerId, wasCreature, target);
            if (wasCreature) {
                gameData.creatureDeathCountThisTurn.merge(controllerId, 1, Integer::sum);
                deathTriggerService.checkAllyCreatureDeathTriggers(gameData, controllerId);
                deathTriggerService.checkAnyCreatureDeathTriggers(gameData);
                deathTriggerService.checkAllyNontokenCreatureDeathTriggers(gameData, controllerId, target.getCard());
                deathTriggerService.checkAnyNontokenCreatureDeathTriggers(gameData, target.getCard());
                deathTriggerService.checkOpponentCreatureDeathTriggers(gameData, controllerId);
                deathTriggerService.checkEquippedCreatureDeathTriggers(gameData, target.getId(), controllerId);
                deathTriggerService.triggerDelayedPoisonOnDeath(gameData, target.getCard().getId(), controllerId);
            }
            if (wasArtifact) {
                deathTriggerService.checkAnyArtifactPutIntoGraveyardFromBattlefieldTriggers(gameData, ownerId, controllerId);
            }
            deathTriggerService.checkEnchantedPermanentDeathTriggers(gameData, target.getId(), controllerId);
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
        String sacrificeLog = creature.getCard().getName() + " is sacrificed (" + removedEquipment.getCard().getName() + " became unattached).";
        gameBroadcastService.logAndBroadcast(gameData, sacrificeLog);
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

                    String revertLog = animatedTarget.getCard().getName() + " is no longer a creature.";
                    gameBroadcastService.logAndBroadcast(gameData, revertLog);
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
     * Checks if the removed permanent had an exile-until-source-leaves tracking entry.
     * If so, returns the exiled card to the battlefield under its owner's control.
     */
    private void handleExileReturnOnLeave(GameData gameData, Permanent removedPermanent) {
        PendingExileReturn pending = gameData.exileReturnOnPermanentLeave.remove(removedPermanent.getId());
        if (pending == null) return;

        Card exiledCard = pending.card();
        UUID ownerId = pending.controllerId();

        // Remove card from exile zone
        List<Card> exiledCards = gameData.playerExiledCards.get(ownerId);
        if (exiledCards != null && exiledCards.remove(exiledCard)) {
            // Return as a new permanent
            Permanent perm = new Permanent(exiledCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, perm);
            String playerName = gameData.playerIdToName.get(ownerId);
            String logEntry = exiledCard.getName() + " returns to the battlefield under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returns from exile (source left battlefield)", gameData.id, exiledCard.getName());
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, exiledCard, null, false);
        } else {
            log.info("Game {} - Exiled card {} no longer in exile zone, return skipped", gameData.id, exiledCard.getName());
        }
    }
}
