package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MayCopyHandlerService {

    private final InputCompletionService inputCompletionService;
    private final GameQueryService gameQueryService;
    private final CloneService cloneService;
    private final StateBasedActionService stateBasedActionService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final TargetLegalityService targetLegalityService;
    private final TriggerCollectionService triggerCollectionService;

    public void handleCopyPermanentOnEnterChoice(GameData gameData, Player player, boolean accepted,
                                                  PendingMayAbility ability, CopyPermanentOnEnterEffect copyEffect) {
        String typeLabel = copyEffect.typeLabel();
        if (accepted) {
            // Collect valid targets (the copying permanent is NOT on the battlefield yet)
            List<UUID> validIds = new ArrayList<>();
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (gameQueryService.matchesPermanentPredicate(gameData, p, copyEffect.filter())) {
                        validIds.add(p.getId());
                    }
                }
            }
            playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validIds, "Choose a " + typeLabel + " to copy.");

            String logEntry = player.getUsername() + " accepts — choosing a " + typeLabel + " to copy.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} accepts copy {}", gameData.id, player.getUsername(), typeLabel);
        } else {
            gameData.interaction.clearPermanentChoiceContext();
            String logEntry = player.getUsername() + " declines to copy a " + typeLabel + ". " + ability.sourceCard().getName() + " enters without copying.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines copy {}", gameData.id, player.getUsername(), typeLabel);

            cloneService.completeCloneEntry(gameData, null);
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

    public void handleCopySpellRetargetChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            String logEntry = player.getUsername() + " keeps the original targets for the copy.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to retarget copy", gameData.id, player.getUsername());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Find the copy on the stack
        UUID copyCardId = ability.targetCardId();
        StackEntry copyEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(copyCardId)) {
                copyEntry = se;
                break;
            }
        }

        if (copyEntry == null) {
            log.info("Game {} - Copy no longer on stack for retarget", gameData.id);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card copiedCard = copyEntry.getCard();
        List<UUID> validTargets = new ArrayList<>();

        if (copiedCard.isNeedsSpellTarget()) {
            // Targets a spell on the stack
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(copyCardId)) continue; // exclude the copy itself
                try {
                    targetLegalityService.validateSpellTargetOnStack(gameData, se.getCard().getId(), copiedCard.getTargetFilter(), copyEntry.getControllerId());
                    validTargets.add(se.getCard().getId());
                } catch (IllegalStateException ignored) {
                    // Invalid target for copied spell filter; skip.
                }
            }
        } else if (copiedCard.isNeedsTarget()) {
            Zone targetZone = copyEntry.getTargetZone() != null ? copyEntry.getTargetZone() : Zone.BATTLEFIELD;

            List<UUID> candidateTargets = new ArrayList<>(gameData.orderedPlayerIds);
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent p : battlefield) {
                    candidateTargets.add(p.getId());
                }
            }

            for (UUID candidate : candidateTargets) {
                try {
                    targetLegalityService.validateSpellTargeting(
                            gameData,
                            copiedCard,
                            candidate,
                            targetZone,
                            copyEntry.getControllerId()
                    );
                    validTargets.add(candidate);
                } catch (IllegalStateException ignored) {
                    // Candidate is not legal for this copied spell.
                }
            }
        }

        if (validTargets.isEmpty()) {
            String logEntry = "No valid targets available for the copy.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - No valid targets for copy retarget", gameData.id);

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SpellRetarget(copyCardId));
        playerInputService.beginPermanentChoice(gameData, ability.controllerId(), validTargets,
                "Choose a new target for the copy of " + copiedCard.getName() + ".");
    }

    public void handleBecomeCopyChoice(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card sourceCard = ability.sourceCard();

        if (!accepted) {
            String logEntry = player.getUsername() + " declines " + sourceCard.getName() + "'s copy ability.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines become-copy ability from {}", gameData.id, player.getUsername(), sourceCard.getName());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Find source permanent by card identity
        Permanent sourcePermanent = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getCard() == sourceCard) {
                    sourcePermanent = p;
                    break;
                }
            }
            if (sourcePermanent != null) break;
        }

        if (sourcePermanent == null) {
            String logEntry = sourceCard.getName() + " is no longer on the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} become-copy source no longer on battlefield", gameData.id, sourceCard.getName());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Find target permanent (stored in targetCardId during resolution queueing)
        UUID targetPermId = ability.targetCardId();
        Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetPermId);
        if (targetPerm == null) {
            String logEntry = sourceCard.getName() + "'s copy target is no longer on the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} become-copy target no longer on battlefield", gameData.id, sourceCard.getName());

            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Apply the copy
        String originalName = sourcePermanent.getCard().getName();
        cloneService.applyCloneCopy(sourcePermanent, targetPerm, null, null);

        // Retain the upkeep copy ability per "except it has this ability"
        Card copiedCard = sourcePermanent.getCard();
        copiedCard.addEffect(EffectSlot.UPKEEP_TRIGGERED, new BecomeCopyOfTargetCreatureEffect());

        String targetName = targetPerm.getCard().getName();
        String logEntry = originalName + " becomes a copy of " + targetName + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} becomes a copy of {}", gameData.id, originalName, targetName);

        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }
}
