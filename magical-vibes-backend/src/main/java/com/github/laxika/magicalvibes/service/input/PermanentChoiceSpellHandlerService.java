package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles permanent choice contexts related to spell casting from non-hand zones
 * and spell retargeting.
 *
 * <p>Covers retargeting spells on the stack, and casting spells from the library,
 * exile, or graveyard that require a permanent/player target.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermanentChoiceSpellHandlerService {

    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;

    public void handleSpellRetarget(GameData gameData, UUID permanentId, PermanentChoiceContext.SpellRetarget retarget) {
        StackEntry targetSpell = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(retarget.spellCardId())) {
                targetSpell = se;
                break;
            }
        }
        if (targetSpell == null) {
            log.info("Game {} - Target spell no longer on stack for retarget", gameData.id);
        } else {
            targetSpell.setTargetPermanentId(permanentId);
            String spellName = targetSpell.isCopy()
                    ? "Copy of " + targetSpell.getCard().getName()
                    : targetSpell.getCard().getName();
            String targetName = getTargetDisplayName(gameData, permanentId);
            String logMsg = spellName + " now targets " + targetName + ".";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            log.info("Game {} - {} retargeted to {}", gameData.id, spellName, targetName);

            // Check becomes-target-of-spell triggers for the new target (e.g. Livewire Lash)
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData, targetSpell);
            if (gameData.interaction.isAwaitingInput()) return;
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleLibraryCastSpellTarget(GameData gameData, UUID permanentId, PermanentChoiceContext.LibraryCastSpellTarget lct) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        boolean isPlayerTarget = gameData.playerIds.contains(permanentId);

        if (target != null || isPlayerTarget) {
            StackEntry entry = new StackEntry(
                    lct.spellType(),
                    lct.cardToCast(),
                    lct.controllerId(),
                    lct.cardToCast().getName(),
                    new ArrayList<>(lct.spellEffects()),
                    0,
                    permanentId,
                    null
            );
            gameData.stack.add(entry);

            gameData.spellsCastThisTurn.merge(lct.controllerId(), 1, Integer::sum);
            gameData.priorityPassedBy.clear();

            String targetName = isPlayerTarget
                    ? gameData.playerIdToName.get(permanentId)
                    : target.getCard().getName();
            String logEntry = lct.cardToCast().getName() + " targets " + targetName + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} cast-from-library targets {}", gameData.id, lct.cardToCast().getName(), targetName);

            triggerCollectionService.checkSpellCastTriggers(gameData, lct.cardToCast(), lct.controllerId(), false);
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        } else {
            graveyardService.addCardToGraveyard(gameData, lct.controllerId(), lct.cardToCast());
            String logEntry = lct.cardToCast().getName() + "'s target is no longer valid. It is put into the graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} cast-from-library target no longer exists", gameData.id, lct.cardToCast().getName());
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleExileCastSpellTarget(GameData gameData, UUID permanentId, PermanentChoiceContext.ExileCastSpellTarget ect) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        boolean isPlayerTarget = gameData.playerIds.contains(permanentId);

        if (target != null || isPlayerTarget) {
            StackEntry entry = new StackEntry(
                    ect.spellType(),
                    ect.cardToCast(),
                    ect.controllerId(),
                    ect.cardToCast().getName(),
                    new ArrayList<>(ect.spellEffects()),
                    0,
                    permanentId,
                    null
            );
            gameData.stack.add(entry);

            gameData.spellsCastThisTurn.merge(ect.controllerId(), 1, Integer::sum);
            gameData.priorityPassedBy.clear();

            String targetName = isPlayerTarget
                    ? gameData.playerIdToName.get(permanentId)
                    : target.getCard().getName();
            String logEntry = ect.cardToCast().getName() + " targets " + targetName + " (Knowledge Pool).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} cast-from-exile targets {}", gameData.id, ect.cardToCast().getName(), targetName);

            triggerCollectionService.checkSpellCastTriggers(gameData, ect.cardToCast(), ect.controllerId(), false);
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        } else {
            graveyardService.addCardToGraveyard(gameData, ect.controllerId(), ect.cardToCast());
            String logEntry = ect.cardToCast().getName() + "'s target is no longer valid. It is put into the graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} cast-from-exile target no longer exists", gameData.id, ect.cardToCast().getName());
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleGraveyardCastSpellTarget(GameData gameData, UUID permanentId, PermanentChoiceContext.GraveyardCastSpellTarget gct) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        boolean isPlayerTarget = gameData.playerIds.contains(permanentId);

        if (target != null || isPlayerTarget) {
            StackEntry entry = new StackEntry(
                    gct.spellType(),
                    gct.cardToCast(),
                    gct.controllerId(),
                    gct.cardToCast().getName(),
                    new ArrayList<>(gct.spellEffects()),
                    0,
                    permanentId,
                    null
            );
            gameData.stack.add(entry);

            gameData.spellsCastThisTurn.merge(gct.controllerId(), 1, Integer::sum);
            gameData.priorityPassedBy.clear();

            String targetName = isPlayerTarget
                    ? gameData.playerIdToName.get(permanentId)
                    : target.getCard().getName();
            String logEntry = gct.cardToCast().getName() + " targets " + targetName + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} cast-from-graveyard targets {}", gameData.id, gct.cardToCast().getName(), targetName);

            triggerCollectionService.checkSpellCastTriggers(gameData, gct.cardToCast(), gct.controllerId(), false);
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        } else {
            graveyardService.addCardToGraveyard(gameData, gct.controllerId(), gct.cardToCast());
            String logEntry = gct.cardToCast().getName() + "'s target is no longer valid. It is put into the graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} cast-from-graveyard target no longer exists", gameData.id, gct.cardToCast().getName());
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        gameData.priorityPassedBy.clear();
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleHandCastSpellTarget(GameData gameData, UUID permanentId, PermanentChoiceContext.HandCastSpellTarget hct) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        boolean isPlayerTarget = gameData.playerIds.contains(permanentId);

        if (target != null || isPlayerTarget) {
            StackEntry entry = new StackEntry(
                    hct.spellType(),
                    hct.cardToCast(),
                    hct.controllerId(),
                    hct.cardToCast().getName(),
                    new ArrayList<>(hct.spellEffects()),
                    0,
                    permanentId,
                    null
            );
            gameData.stack.add(entry);

            gameData.spellsCastThisTurn.merge(hct.controllerId(), 1, Integer::sum);
            gameData.priorityPassedBy.clear();

            String targetName = isPlayerTarget
                    ? gameData.playerIdToName.get(permanentId)
                    : target.getCard().getName();
            String logEntry = hct.cardToCast().getName() + " targets " + targetName + " (Wild Evocation).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} cast-from-hand targets {}", gameData.id, hct.cardToCast().getName(), targetName);

            triggerCollectionService.checkSpellCastTriggers(gameData, hct.cardToCast(), hct.controllerId(), false);
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        } else {
            graveyardService.addCardToGraveyard(gameData, hct.controllerId(), hct.cardToCast());
            String logEntry = hct.cardToCast().getName() + "'s target is no longer valid. It is put into the graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} cast-from-hand target no longer exists", gameData.id, hct.cardToCast().getName());
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
