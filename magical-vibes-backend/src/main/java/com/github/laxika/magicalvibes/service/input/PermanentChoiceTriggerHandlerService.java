package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingCapriciousEfreetState;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles permanent choice contexts related to triggered ability targeting.
 *
 * <p>Covers spell-target triggers, discard triggers, death triggers,
 * may-ability triggers, attack triggers, emblem triggers, and upkeep copy triggers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermanentChoiceTriggerHandlerService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;
    private final InputCompletionService inputCompletionService;

    public void handleSpellTargetTrigger(GameData gameData, UUID permanentId, PermanentChoiceContext.SpellTargetTriggerAnyTarget stt) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                stt.sourceCard(),
                stt.controllerId(),
                stt.sourceCard().getName() + "'s ability",
                new ArrayList<>(stt.effects())
        );
        entry.setTargetPermanentId(permanentId);
        gameData.stack.add(entry);

        String targetName = getTargetDisplayName(gameData, permanentId);
        String logEntry = stt.sourceCard().getName() + "'s triggered ability targets " + targetName + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} spell-target trigger targets {}", gameData.id, stt.sourceCard().getName(), targetName);

        if (!gameData.pendingSpellTargetTriggers.isEmpty()) {
            triggerCollectionService.processNextSpellTargetTrigger(gameData);
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        gameData.priorityPassedBy.clear();
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleDiscardTrigger(GameData gameData, UUID permanentId, PermanentChoiceContext.DiscardTriggerAnyTarget dtt) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                dtt.discardedCard(),
                dtt.controllerId(),
                dtt.discardedCard().getName() + "'s ability",
                new ArrayList<>(dtt.effects())
        );
        entry.setTargetPermanentId(permanentId);
        gameData.stack.add(entry);

        String targetName = getTargetDisplayName(gameData, permanentId);
        String logEntry = dtt.discardedCard().getName() + "'s discard trigger targets " + targetName + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} discard trigger targets {}", gameData.id, dtt.discardedCard().getName(), targetName);

        if (!gameData.pendingDiscardSelfTriggers.isEmpty()) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
            return;
        }

        if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        gameData.priorityPassedBy.clear();
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleDeathTrigger(GameData gameData, UUID permanentId, PermanentChoiceContext.DeathTriggerTarget dtt) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                dtt.dyingCard(),
                dtt.controllerId(),
                dtt.dyingCard().getName() + "'s ability",
                new ArrayList<>(dtt.effects())
        );
        entry.setTargetPermanentId(permanentId);
        gameData.stack.add(entry);

        String targetName = getTargetDisplayName(gameData, permanentId);
        String logEntry = dtt.dyingCard().getName() + "'s death trigger targets " + targetName + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} death trigger targets {}", gameData.id, dtt.dyingCard().getName(), targetName);

        if (!gameData.pendingDeathTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextDeathTriggerTarget(gameData);
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        gameData.priorityPassedBy.clear();
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleMayAbilityTrigger(GameData gameData, UUID permanentId, PermanentChoiceContext.MayAbilityTriggerTarget mat) {
        // CR 603.5 — resolution-time target selection: the target was chosen during
        // resolution of a MayEffect on the stack.  Set it on the pending entry and
        // resume the effect resolution loop.
        if (gameData.resolvedMayTargetingEntry != null) {
            StackEntry pendingEntry = gameData.resolvedMayTargetingEntry;
            gameData.resolvedMayTargetingEntry = null;
            pendingEntry.setTargetPermanentId(permanentId);

            Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
            boolean isPlayerTarget = gameData.playerIds.contains(permanentId);
            if (isPlayerTarget) {
                String playerName = gameData.playerIdToName.get(permanentId);
                gameBroadcastService.logAndBroadcast(gameData, mat.sourceCard().getName() + "'s ability targets " + playerName + ".");
            } else if (target != null) {
                gameBroadcastService.logAndBroadcast(gameData, mat.sourceCard().getName() + "'s ability targets " + target.getCard().getName() + ".");
            }

            gameData.resolvedMayAccepted = true;
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(gameData, gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex);
            }
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        boolean isPlayerTarget = gameData.playerIds.contains(permanentId);

        if (target != null || isPlayerTarget) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    mat.sourceCard(),
                    mat.controllerId(),
                    mat.sourceCard().getName() + "'s ability",
                    new ArrayList<>(mat.effects())
            );
            entry.setTargetPermanentId(permanentId);
            gameData.stack.add(entry);

            if (isPlayerTarget) {
                String playerName = gameData.playerIdToName.get(permanentId);
                String logEntry = mat.sourceCard().getName() + "'s ability targets " + playerName + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} may-ability trigger targets player {}", gameData.id, mat.sourceCard().getName(), playerName);
            } else {
                String logEntry = mat.sourceCard().getName() + "'s ability targets " + target.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} may-ability trigger targets {}", gameData.id, mat.sourceCard().getName(), target.getCard().getName());
            }
        } else {
            String logEntry = mat.sourceCard().getName() + "'s ability has no valid target.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} may-ability trigger target no longer exists", gameData.id, mat.sourceCard().getName());
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleAttackTrigger(GameData gameData, UUID permanentId, PermanentChoiceContext.AttackTriggerTarget att) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target != null) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    att.sourceCard(),
                    att.controllerId(),
                    att.sourceCard().getName() + "'s ability",
                    new ArrayList<>(att.effects()),
                    null,
                    att.sourcePermanentId()
            );
            entry.setTargetPermanentId(permanentId);
            gameData.stack.add(entry);

            String logEntry = att.sourceCard().getName() + "'s ability targets " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} attack trigger targets {}", gameData.id, att.sourceCard().getName(), target.getCard().getName());
        } else {
            String logEntry = att.sourceCard().getName() + "'s ability has no valid target.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} attack trigger target no longer exists", gameData.id, att.sourceCard().getName());
        }

        if (!gameData.pendingAttackTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextAttackTriggerTarget(gameData);
            return;
        }

        gameData.priorityPassedBy.clear();
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleEmblemTrigger(GameData gameData, UUID permanentId, PermanentChoiceContext.EmblemTriggerTarget ett) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target != null) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ett.sourceCard(),
                    ett.controllerId(),
                    ett.emblemDescription() + "'s ability",
                    new ArrayList<>(ett.effects())
            );
            entry.setTargetPermanentId(permanentId);
            gameData.stack.add(entry);

            String logEntry = ett.emblemDescription() + "'s ability targets " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} emblem trigger targets {}", gameData.id, ett.emblemDescription(), target.getCard().getName());
        } else {
            String logEntry = ett.emblemDescription() + "'s ability has no valid target.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} emblem trigger target no longer exists", gameData.id, ett.emblemDescription());
        }

        if (!gameData.pendingEmblemTriggerTargets.isEmpty()) {
            triggerCollectionService.processNextEmblemTriggerTarget(gameData);
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        gameData.priorityPassedBy.clear();
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleUpkeepCopyTrigger(GameData gameData, UUID permanentId, PermanentChoiceContext.UpkeepCopyTriggerTarget uct) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target != null) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    uct.sourceCard(),
                    uct.controllerId(),
                    uct.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new BecomeCopyOfTargetCreatureEffect())),
                    null,
                    uct.sourcePermanentId()
            );
            entry.setTargetPermanentId(permanentId);
            gameData.stack.add(entry);

            String logEntry = uct.sourceCard().getName() + "'s ability targets " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} upkeep copy trigger targets {}", gameData.id, uct.sourceCard().getName(), target.getCard().getName());
        } else {
            String logEntry = uct.sourceCard().getName() + "'s ability has no valid target.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} upkeep copy trigger target no longer exists", gameData.id, uct.sourceCard().getName());
        }

        if (!gameData.pendingUpkeepCopyTargets.isEmpty()) {
            turnProgressionService.processNextUpkeepCopyTarget(gameData);
            return;
        }

        if (!gameData.pendingCapriciousEfreetTargets.isEmpty()) {
            turnProgressionService.processNextCapriciousEfreetTarget(gameData);
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleCapriciousEfreetOwnTarget(GameData gameData, UUID permanentId, PermanentChoiceContext.CapriciousEfreetOwnTarget ceo) {
        // Step 1 complete: own nonland permanent chosen. Now collect opponent nonland permanents for step 2.
        UUID controllerId = ceo.controllerId();
        List<UUID> validOpponentTargets = new ArrayList<>();

        for (UUID pid : gameData.orderedPlayerIds) {
            if (pid.equals(controllerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (!p.getCard().hasType(CardType.LAND)) {
                    validOpponentTargets.add(p.getId());
                }
            }
        }

        if (validOpponentTargets.isEmpty()) {
            // No opponent nonland permanents — push onto stack with just the own target
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ceo.sourceCard(),
                    controllerId,
                    ceo.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new DestroyOneOfTargetsAtRandomEffect())),
                    0,
                    List.of(permanentId)
            );
            gameData.stack.add(entry);

            Permanent ownTarget = gameQueryService.findPermanentById(gameData, permanentId);
            String ownName = ownTarget != null ? ownTarget.getCard().getName() : permanentId.toString();
            String logEntry = ceo.sourceCard().getName() + "'s ability targets " + ownName + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} upkeep trigger targets own {} (no opponent targets available)",
                    gameData.id, ceo.sourceCard().getName(), ownName);

            continueAfterCapriciousEfreet(gameData);
            return;
        }

        // Store state for step 2 and present opponent target selection
        gameData.pendingCapriciousEfreetState = new PendingCapriciousEfreetState(
                ceo.sourceCard(), controllerId, ceo.sourcePermanentId(), permanentId);

        int maxOpponentTargets = Math.min(2, validOpponentTargets.size());
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, validOpponentTargets,
                maxOpponentTargets, ceo.sourceCard().getName()
                        + " — Choose up to 2 nonland permanents you don't control.");

        log.info("Game {} - {} upkeep trigger awaiting opponent target selection (up to {})",
                gameData.id, ceo.sourceCard().getName(), maxOpponentTargets);
    }

    void continueAfterCapriciousEfreet(GameData gameData) {
        if (!gameData.pendingCapriciousEfreetTargets.isEmpty()) {
            turnProgressionService.processNextCapriciousEfreetTarget(gameData);
            return;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    private String getTargetDisplayName(GameData gameData, UUID targetId) {
        String playerName = gameData.playerIdToName.get(targetId);
        if (playerName != null) return playerName;

        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetId)) return se.getCard().getName();
        }

        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getId().equals(targetId)) return p.getCard().getName();
            }
        }

        Card graveyardCard = gameQueryService.findCardInGraveyardById(gameData, targetId);
        if (graveyardCard != null) return graveyardCard.getName();

        return targetId.toString();
    }
}
