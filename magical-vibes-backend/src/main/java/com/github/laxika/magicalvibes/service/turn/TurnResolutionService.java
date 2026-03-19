package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ControlTargetPlayerNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurnResolutionService {

    private final CombatService combatService;
    private final GameBroadcastService gameBroadcastService;
    private final AuraAttachmentService auraAttachmentService;
    private final TurnCleanupService turnCleanupService;
    private final ExileService exileService;

    @HandlesEffect(ExtraTurnEffect.class)
    void resolveExtraTurn(GameData gameData, StackEntry entry, ExtraTurnEffect effect) {
        UUID targetPlayerId = resolveTargetPlayer(gameData, entry);
        if (targetPlayerId == null) {
            return;
        }

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        for (int i = 0; i < effect.count(); i++) {
            gameData.extraTurns.addFirst(targetPlayerId);
        }

        String logEntry = playerName + " takes " + effect.count() + " extra "
                + pluralize("turn", effect.count()) + " after this one.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} granted {} extra turn(s)", gameData.id, playerName, effect.count());
    }

    @HandlesEffect(EndTurnEffect.class)
    void resolveEndTurn(GameData gameData) {
        // Rule 723.1a: Triggered abilities that haven't been put on the stack yet cease to exist
        gameData.pendingMayAbilities.clear();

        // Rule 723.1b: Exile every object on the stack
        exileStackEntries(gameData);

        // Rule 723.1c: Clear combat state
        clearCombatState(gameData);

        // Rule 723.1d: Skip to cleanup step
        skipToCleanupStep(gameData);

        // Flag so resolveTopOfStack exiles the resolving card instead of graveyard (rule 723.1b)
        gameData.endTurnRequested = true;

        gameBroadcastService.logAndBroadcast(gameData, "The turn ends.");
        log.info("Game {} - End the turn effect resolved, skipping to cleanup", gameData.id);
    }

    @HandlesEffect(ControlTargetPlayerNextTurnEffect.class)
    void resolveControlTargetPlayerNextTurn(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = resolveTargetPlayer(gameData, entry);
        if (targetPlayerId == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        gameData.pendingTurnControl.put(targetPlayerId, controllerId);

        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = controllerName + " will control " + targetName + " during their next turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} will control {} during their next turn (Mindslaver)",
                gameData.id, controllerName, targetName);
    }

    @HandlesEffect(AdditionalCombatMainPhaseEffect.class)
    void resolveAdditionalCombatMainPhase(GameData gameData, StackEntry entry, AdditionalCombatMainPhaseEffect effect) {
        if (effect.count() <= 0) {
            return;
        }

        gameData.additionalCombatMainPhasePairs += effect.count();

        String logEntry;
        if (effect.count() == 1) {
            logEntry = "After this main phase, there is an additional combat phase followed by an additional main phase.";
        } else {
            logEntry = "After this main phase, there are " + effect.count()
                    + " additional combat " + pluralize("phase", effect.count())
                    + " followed by additional main " + pluralize("phase", effect.count()) + ".";
        }
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} queued {} additional combat/main phase pair(s)",
                gameData.id, entry.getCard().getName(), effect.count());
    }

    private UUID resolveTargetPlayer(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return null;
        }
        return targetPlayerId;
    }

    private void exileStackEntries(GameData gameData) {
        // The resolving spell (e.g. Time Stop) is already removed from the stack by resolveTopOfStack,
        // so we only need to handle remaining entries.
        List<StackEntry> remaining = new ArrayList<>(gameData.stack);
        gameData.stack.clear();

        Set<StackEntryType> spellTypes = Set.of(
                StackEntryType.CREATURE_SPELL, StackEntryType.INSTANT_SPELL,
                StackEntryType.SORCERY_SPELL, StackEntryType.ENCHANTMENT_SPELL,
                StackEntryType.ARTIFACT_SPELL, StackEntryType.PLANESWALKER_SPELL
        );

        for (StackEntry se : remaining) {
            if (spellTypes.contains(se.getEntryType())) {
                Card card = se.getCard();
                exileService.exileCard(gameData, se.getControllerId(), card);
                gameBroadcastService.logAndBroadcast(gameData, card.getName() + " is exiled.");
                log.info("Game {} - {} exiled from stack (end the turn)", gameData.id, card.getName());
            }
            // Triggered/activated abilities just cease to exist
        }
    }

    private void clearCombatState(GameData gameData) {
        combatService.clearCombatState(gameData);
        gameData.permanentsToSacrificeAtEndOfCombat.clear();
        gameData.pendingTokenExilesAtEndOfCombat.clear();
    }

    private void skipToCleanupStep(GameData gameData) {
        gameData.currentStep = TurnStep.CLEANUP;
        turnCleanupService.resetEndOfTurnModifiers(gameData);
        auraAttachmentService.returnStolenCreatures(gameData, true);
        gameData.priorityPassedBy.clear();
    }

    private static String pluralize(String word, int count) {
        return count == 1 ? word : word + "s";
    }
}

