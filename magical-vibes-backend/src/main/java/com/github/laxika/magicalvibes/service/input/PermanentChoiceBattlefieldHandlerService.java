package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.WarpWorldEnchantmentPlacement;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.DeathTriggerService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.combat.DamageResolutionService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles permanent choice contexts related to battlefield manipulation.
 *
 * <p>Covers clone copies, aura grafting, legend rule, sacrificing creatures,
 * activated ability cost choices, bouncing, damage prevention source choices,
 * sacrifice-for-divided-damage, and aura ETB placement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermanentChoiceBattlefieldHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final DeathTriggerService deathTriggerService;
    private final CloneService cloneService;
    private final WarpWorldService warpWorldService;
    private final GameBroadcastService gameBroadcastService;
    private final AbilityActivationService abilityActivationService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final StateBasedActionService stateBasedActionService;
    private final TriggerCollectionService triggerCollectionService;
    private final CreatureControlService creatureControlService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;
    private final DamageResolutionService damageResolutionService;

    public void handleCloneCopy(GameData gameData, UUID permanentId) {
        Permanent targetPerm = gameQueryService.findPermanentById(gameData, permanentId);
        if (targetPerm == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        cloneService.completeCloneEntry(gameData, permanentId);

        if (!gameData.interaction.isAwaitingInput()) {
            stateBasedActionService.performStateBasedActions(gameData);

            if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
                triggerCollectionService.processNextDeathTriggerTarget(gameData);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            }

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    public void handleAuraGraft(GameData gameData, UUID permanentId, PermanentChoiceContext.AuraGraft auraGraft) {
        Permanent aura = gameQueryService.findPermanentById(gameData, auraGraft.auraPermanentId());
        if (aura == null) {
            throw new IllegalStateException("Aura permanent no longer exists");
        }

        Permanent newTarget = gameQueryService.findPermanentById(gameData, permanentId);
        if (newTarget == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        aura.setAttachedTo(permanentId);

        String logEntry = aura.getCard().getName() + " is now attached to " + newTarget.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} reattached to {}", gameData.id, aura.getCard().getName(), newTarget.getCard().getName());

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleLegendRule(GameData gameData, UUID playerId, UUID permanentId, PermanentChoiceContext.LegendRule legendRule) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Permanent> toRemove = new ArrayList<>();
        for (Permanent perm : battlefield) {
            if (perm.getCard().getName().equals(legendRule.cardName()) && !perm.getId().equals(permanentId)) {
                toRemove.add(perm);
            }
        }
        for (Permanent perm : toRemove) {
            boolean wasCreature = gameQueryService.isCreature(gameData, perm);
            battlefield.remove(perm);
            boolean wentToGraveyard = graveyardService.addCardToGraveyard(gameData, playerId, perm.getOriginalCard(), Zone.BATTLEFIELD);
            if (wentToGraveyard) {
                deathTriggerService.collectDeathTrigger(gameData, perm.getCard(), playerId, wasCreature, perm);
                if (wasCreature) {
                    deathTriggerService.checkAllyCreatureDeathTriggers(gameData, playerId);
                }
            }
            String logEntry = perm.getCard().getName() + " is put into the graveyard (legend rule).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} sent to graveyard by legend rule", gameData.id, perm.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleSacrificeCreature(GameData gameData, UUID permanentId, PermanentChoiceContext.SacrificeCreature sacrificeCreature) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        UUID sacrificingPlayerId = sacrificeCreature.sacrificingPlayerId();
        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
        String logEntry = playerName + " sacrifices " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, target.getCard().getName());

        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleActivatedAbilityCostChoice(GameData gameData, Player player, UUID permanentId, PermanentChoiceContext.ActivatedAbilityCostChoice costChoice) {
        abilityActivationService.completeActivatedAbilityCostChoice(gameData, player, costChoice, permanentId);
    }

    public void handleBounceCreature(GameData gameData, UUID permanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target creature no longer exists");
        }

        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            permanentRemovalService.removeOrphanedAuras(gameData);

            String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to owner's hand by bounce effect", gameData.id, target.getCard().getName());
        }

        stateBasedActionService.performStateBasedActions(gameData);

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleBounceOwnPermanentOrSacrificeSelf(GameData gameData, UUID permanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            permanentRemovalService.removeOrphanedAuras(gameData);

            String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to owner's hand by bounce-or-sacrifice effect", gameData.id, target.getCard().getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    public void handlePreventDamageSourceChoice(GameData gameData, UUID permanentId, PermanentChoiceContext.PreventDamageSourceChoice preventSource) {
        Permanent chosenPermanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosenPermanent == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        UUID controllerId = preventSource.controllerId();
        gameData.playerSourceDamagePreventionIds
                .computeIfAbsent(controllerId, k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(permanentId);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = "All damage " + chosenPermanent.getCard().getName() + " would deal to " + playerName + " is prevented this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} chose {} as prevented damage source", gameData.id, playerName, chosenPermanent.getCard().getName());

        stateBasedActionService.performStateBasedActions(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleSacrificeArtifactForDividedDamage(GameData gameData, UUID permanentId, PermanentChoiceContext.SacrificeArtifactForDividedDamage sadd) {
        Permanent artifactToSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (artifactToSacrifice == null) {
            throw new IllegalStateException("Artifact permanent no longer exists");
        }

        permanentRemovalService.removePermanentToGraveyard(gameData, artifactToSacrifice);

        String playerName = gameData.playerIdToName.get(sadd.controllerId());
        String logEntry = playerName + " sacrifices " + artifactToSacrifice.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {} for divided damage", gameData.id, playerName, artifactToSacrifice.getCard().getName());

        damageResolutionService.dealDividedDamageToAnyTargets(
                gameData, sadd.sourceCard(), sadd.controllerId(), sadd.damageAssignments());

        gameData.pendingETBDamageAssignments = Map.of();

        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handlePendingAuraPlacement(GameData gameData, UUID playerId, UUID permanentId) {
        Card auraCard = gameData.interaction.consumePendingAuraCard();

        Permanent enchantTarget = gameQueryService.findPermanentById(gameData, permanentId);
        if (enchantTarget == null) {
            throw new IllegalStateException("Target permanent no longer exists");
        }

        if (gameData.warpWorldOperation.sourceName != null) {
            gameData.warpWorldOperation.pendingEnchantmentPlacements.add(
                    new WarpWorldEnchantmentPlacement(playerId, auraCard, enchantTarget.getId())
            );

            if (!gameData.warpWorldOperation.pendingAuraChoices.isEmpty()) {
                warpWorldService.beginNextPendingWarpWorldAuraChoice(gameData);
                return;
            }
            warpWorldService.placePendingWarpWorldEnchantments(gameData);
            if (!gameData.pendingLibraryBottomReorders.isEmpty()) {
                warpWorldService.beginNextPendingLibraryBottomReorder(gameData);
                return;
            }
            warpWorldService.finalizePendingWarpWorld(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        } else {
            Permanent auraPerm = new Permanent(auraCard);
            auraPerm.setAttachedTo(enchantTarget.getId());
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, auraPerm);

            boolean hasControlEffect = auraCard.getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
            if (hasControlEffect) {
                creatureControlService.stealPermanent(gameData, playerId, enchantTarget);
            }

            String playerName = gameData.playerIdToName.get(playerId);
            String logEntry = auraCard.getName() + " enters the battlefield attached to " + enchantTarget.getCard().getName() + " under " + playerName + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} puts {} onto battlefield attached to {}",
                    gameData.id, playerName, auraCard.getName(), enchantTarget.getCard().getName());
        }

        turnProgressionService.resolveAutoPass(gameData);
    }
}
