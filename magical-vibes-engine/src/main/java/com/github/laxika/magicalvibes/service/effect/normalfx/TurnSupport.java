package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.rules.TurnEvent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.rules.StepEvent;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;

/**
 * Shared turn helpers used by every "normal" Turn effect handler.
 *
 * <p>Extracted verbatim from {@code TurnResolutionService}; behavior is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TurnSupport {

    private final CombatService combatService;
    private final GameLogService gameLogService;
    private final CreatureControlService creatureControlService;
    private final TurnCleanupService turnCleanupService;
    private final ExileService exileService;

    @Autowired
    @Lazy
    private PlayerInputService playerInputService;

    public UUID resolveTargetPlayer(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return null;
        }
        return targetPlayerId;
    }

    public void exileStackEntries(GameData gameData) {
        exileStackEntries(gameData, "end the turn");
    }

    public void exileStackEntries(GameData gameData, String reason) {
        // The resolving spell (e.g. Time Stop) is already removed from the stack by resolveTopOfStack,
        // so we only need to handle remaining entries.
        List<StackEntry> remaining = new ArrayList<>(gameData.stack);
        gameData.stack.clear();

        Set<StackEntryType> spellTypes = Set.of(
                StackEntryType.CREATURE_SPELL, StackEntryType.INSTANT_SPELL,
                StackEntryType.SORCERY_SPELL, StackEntryType.ENCHANTMENT_SPELL,
                StackEntryType.ARTIFACT_SPELL, StackEntryType.PLANESWALKER_SPELL,
                StackEntryType.BATTLE_SPELL
        );

        for (StackEntry se : remaining) {
            if (spellTypes.contains(se.getEntryType()) && !se.isCopy()) {
                Card card = se.getPhysicalCard();
                exileService.exileCard(gameData, se.getOwnerId(), card);
                gameLogService.append(gameData, GameLog.cardThen(card, " is exiled."));
                log.info("Game {} - {} exiled from stack ({})", gameData.id, card.getName(), reason);
            }
            // Triggered/activated abilities just cease to exist
        }
    }

    public void clearCombatState(GameData gameData) {
        gameData.rulesEvents.occur(new TurnEvent.StepEnded(TurnStep.END_OF_COMBAT, gameData.activePlayerId));
        combatService.clearCombatState(gameData);
    }

    public void endCombatPhase(GameData gameData) {
        clearCombatState(gameData);
        gameData.onlyAggressiveCreaturesCanAttackThisCombat = false;
        if (gameData.mindControlUntilEndOfCombat) {
            gameData.mindControlledPlayerId = null;
            gameData.mindControllerPlayerId = null;
            gameData.mindControlUntilEndOfCombat = false;
        }
        if (gameData.additionalCombatReturnActivePlayerId != null) {
            gameData.activePlayerId = gameData.additionalCombatReturnActivePlayerId;
            gameData.additionalCombatReturnActivePlayerId = null;
        }
        turnCleanupService.drainManaPools(gameData);
        gameData.revertableManaActivations.clear();
        gameData.resetPriorityToActivePlayer();
        gameData.interaction.clearAwaitingInput();
        gameData.currentStep = TurnStep.POSTCOMBAT_MAIN;
    }

    public void skipToCleanupStep(GameData gameData) {
        if (gameData.currentExtraTurnSequence != null) {
            gameData.endDelayedAbilitiesOfExtraTurn(gameData.currentExtraTurnSequence);
        }
        gameData.currentStep = TurnStep.CLEANUP;
        creatureControlService.reconcileControl(gameData);
        gameData.controlLossUnattachTriggers.clear();
        gameData.resetPriorityToActivePlayer();

        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null) {
            turnCleanupService.applyCleanupResets(gameData);
            return;
        }
        List<Card> hand = gameData.playerHands.get(activePlayerId);
        int maxHandSize = Math.max(turnCleanupService.getMaxHandSize(gameData, activePlayerId), 0);
        if (hand != null && hand.size() > maxHandSize
                && !turnCleanupService.hasNoMaximumHandSize(gameData, activePlayerId)) {
            gameData.cleanupDiscardPending = true;
            gameData.discardCausedByOpponent = false;
            playerInputService.beginDiscardChoice(gameData, activePlayerId, hand.size() - maxHandSize);
        } else {
            // The cleanup step begins: the delayed abilities that wait for it trigger (CR 514.3a).
            gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(
                    new StepEvent(StepTrigger.Moment.CLEANUP, gameData.activePlayerId)));
            turnCleanupService.applyCleanupResets(gameData);
        }
    }

    public static String pluralize(String word, int count) {
        return count == 1 ? word : word + "s";
    }
}
