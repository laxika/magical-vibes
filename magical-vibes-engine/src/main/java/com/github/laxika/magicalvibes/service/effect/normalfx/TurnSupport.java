package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.action.ExileTokenAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private final GameBroadcastService gameBroadcastService;
    private final CreatureControlService creatureControlService;
    private final TurnCleanupService turnCleanupService;
    private final ExileService exileService;

    public UUID resolveTargetPlayer(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return null;
        }
        return targetPlayerId;
    }

    public void exileStackEntries(GameData gameData) {
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

    public void clearCombatState(GameData gameData) {
        combatService.clearCombatState(gameData);
        gameData.clearDelayedActions(SacrificeAtEndOfCombat.class);
        gameData.clearDelayedActions(ExileTokenAtEndOfCombat.class);
    }

    public void skipToCleanupStep(GameData gameData) {
        gameData.currentStep = TurnStep.CLEANUP;
        turnCleanupService.resetEndOfTurnModifiers(gameData);
        creatureControlService.reconcileControl(gameData);
        gameData.priorityPassedBy.clear();
    }

    public static String pluralize(String word, int count) {
        return count == 1 ? word : word + "s";
    }
}
