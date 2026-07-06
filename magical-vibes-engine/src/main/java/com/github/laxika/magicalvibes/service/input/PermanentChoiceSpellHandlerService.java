package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileCastTargetSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ImprovisationCapstoneCastSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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
public class PermanentChoiceSpellHandlerService {

    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    // @Lazy breaks the cycle: PermanentChoiceSpellHandlerService → ImprovisationCapstoneCastSupport →
    // PlayerInputService → InteractionHandlerRegistry → ImprovisationCapstoneCastChoiceInteractionHandler
    // → ImprovisationCapstoneCastSupport.
    private final ImprovisationCapstoneCastSupport improvisationCapstoneCastSupport;
    private final ExileCastTargetSupport exileCastTargetSupport;

    public PermanentChoiceSpellHandlerService(GameQueryService gameQueryService,
                                              GraveyardService graveyardService,
                                              GameBroadcastService gameBroadcastService,
                                              TriggerCollectionService triggerCollectionService,
                                              PlayerInputService playerInputService,
                                              TurnProgressionService turnProgressionService,
                                              @Lazy ImprovisationCapstoneCastSupport improvisationCapstoneCastSupport,
                                              ExileCastTargetSupport exileCastTargetSupport) {
        this.gameQueryService = gameQueryService;
        this.graveyardService = graveyardService;
        this.gameBroadcastService = gameBroadcastService;
        this.triggerCollectionService = triggerCollectionService;
        this.playerInputService = playerInputService;
        this.turnProgressionService = turnProgressionService;
        this.improvisationCapstoneCastSupport = improvisationCapstoneCastSupport;
        this.exileCastTargetSupport = exileCastTargetSupport;
    }

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
            targetSpell.setTargetId(permanentId);
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

            gameData.recordSpellCast(lct.controllerId(), lct.cardToCast());
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
        // Multi-target spells (e.g. Echocasting Symposium: target player + target creature you control)
        // collect their targets one slot at a time, in the card's declared order.
        if (ect.cardToCast().getMaxTargets() > 1) {
            handleMultiTargetExileCast(gameData, permanentId, ect);
            return;
        }

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
            entry.setCopy(ect.copy());
            gameData.stack.add(entry);

            gameData.recordSpellCast(ect.controllerId(), ect.cardToCast());
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

        resumeAfterExileCast(gameData, ect.controllerId());
    }

    /**
     * Collects the targets of a multi-target spell cast from exile one slot at a time. Each response
     * fills the next declared target slot; while slots remain, computes the legal candidates for the
     * following slot and prompts again. Once every slot is filled the spell is put on the stack with
     * its ordered target list.
     */
    private void handleMultiTargetExileCast(GameData gameData, UUID permanentId,
                                            PermanentChoiceContext.ExileCastSpellTarget ect) {
        Card card = ect.cardToCast();
        List<UUID> chosen = new ArrayList<>(ect.chosenTargets());
        chosen.add(permanentId);

        int totalSlots = card.getMaxTargets();
        if (chosen.size() < totalSlots) {
            List<UUID> nextCandidates = exileCastTargetSupport.nextSlotCandidates(gameData, card, ect.controllerId(), chosen);
            if (nextCandidates.isEmpty()) {
                // The full target set was pre-validated before prompting, so this only happens if a
                // remaining slot's targets vanished mid-selection. The spell can't be legally cast:
                // a copy ceases to exist (CR 707.10a), a real card goes to its owner's graveyard.
                if (!ect.copy()) {
                    graveyardService.addCardToGraveyard(gameData, ect.controllerId(), card);
                }
                String logEntry = card.getName() + "'s targets are no longer valid.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} multi-target cast-from-exile has no legal target for a remaining slot",
                        gameData.id, card.getName());
                resumeAfterExileCast(gameData, ect.controllerId());
                return;
            }

            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ExileCastSpellTarget(
                    card, ect.controllerId(), ect.spellEffects(), ect.spellType(), ect.copy(), chosen));
            playerInputService.beginPermanentChoice(gameData, ect.controllerId(), nextCandidates,
                    "Choose a target for " + card.getName() + ".");
            gameBroadcastService.logAndBroadcast(gameData,
                    card.getName() + " targets " + getTargetDisplayName(gameData, permanentId) + " — choosing next target.");
            return;
        }

        // Every target slot is filled — put the spell on the stack preserving the declared order.
        StackEntry entry = new StackEntry(
                ect.spellType(),
                card,
                ect.controllerId(),
                card.getName(),
                new ArrayList<>(ect.spellEffects()),
                0,
                chosen
        );
        entry.setCopy(ect.copy());
        gameData.stack.add(entry);

        gameData.recordSpellCast(ect.controllerId(), card);
        gameData.priorityPassedBy.clear();

        List<String> targetNames = chosen.stream().map(id -> getTargetDisplayName(gameData, id)).toList();
        String logEntry = card.getName() + " targets " + String.join(", ", targetNames) + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} multi-target cast-from-exile targets {}", gameData.id, card.getName(), targetNames);

        triggerCollectionService.checkSpellCastTriggers(gameData, card, ect.controllerId(), false);
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);

        resumeAfterExileCast(gameData, ect.controllerId());
    }

    /**
     * Resumes turn flow after a spell cast from exile has been placed on the stack (or fizzled).
     * Improvisation Capstone casts a batch of exiled spells; a targeted one pauses for target
     * selection, so resume casting the remainder of the queue before yielding priority.
     */
    private void resumeAfterExileCast(GameData gameData, UUID controllerId) {
        if (!gameData.pendingImprovisationCapstoneCastQueue.isEmpty()) {
            improvisationCapstoneCastSupport.castNextFromQueue(gameData, controllerId);
            return;
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

            gameData.recordSpellCast(gct.controllerId(), gct.cardToCast());
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

            gameData.recordSpellCast(hct.controllerId(), hct.cardToCast());
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
