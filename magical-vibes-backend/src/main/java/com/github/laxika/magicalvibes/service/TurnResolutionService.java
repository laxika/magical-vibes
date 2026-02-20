package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
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
public class TurnResolutionService implements EffectHandlerProvider {

    private static final Set<StackEntryType> SPELL_TYPES = Set.of(
            StackEntryType.CREATURE_SPELL, StackEntryType.INSTANT_SPELL,
            StackEntryType.SORCERY_SPELL, StackEntryType.ENCHANTMENT_SPELL,
            StackEntryType.ARTIFACT_SPELL, StackEntryType.PLANESWALKER_SPELL
    );

    private final GameHelper gameHelper;
    private final CombatService combatService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(EndTurnEffect.class,
                (gd, entry, effect) -> resolveEndTurn(gd, entry));
        registry.register(ExtraTurnEffect.class,
                (gd, entry, effect) -> resolveExtraTurn(gd, entry, (ExtraTurnEffect) effect));
        registry.register(AdditionalCombatMainPhaseEffect.class,
                (gd, entry, effect) -> resolveAdditionalCombatMainPhase(gd, entry, (AdditionalCombatMainPhaseEffect) effect));
    }

    private void resolveExtraTurn(GameData gameData, StackEntry entry, ExtraTurnEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        for (int i = 0; i < effect.count(); i++) {
            gameData.extraTurns.addFirst(targetPlayerId);
        }

        String logEntry = playerName + " takes " + effect.count() + " extra turn"
                + (effect.count() > 1 ? "s" : "") + " after this one.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} granted {} extra turn(s)", gameData.id, playerName, effect.count());
    }

    private void resolveEndTurn(GameData gameData, StackEntry entry) {
        // Rule 723.1a: Triggered abilities that haven't been put on the stack yet cease to exist
        gameData.pendingMayAbilities.clear();

        // Rule 723.1b: Exile every object on the stack.
        // The resolving spell (Time Stop) is already removed from the stack by resolveTopOfStack,
        // so we only need to handle remaining entries.
        List<StackEntry> remaining = new ArrayList<>(gameData.stack);
        gameData.stack.clear();

        for (StackEntry se : remaining) {
            if (SPELL_TYPES.contains(se.getEntryType())) {
                Card card = se.getCard();
                gameData.playerExiledCards.get(se.getControllerId()).add(card);
                String logEntry = card.getName() + " is exiled.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} exiled from stack (end the turn)", gameData.id, card.getName());
            }
            // Triggered/activated abilities just cease to exist
        }

        // Clear combat state if we're ending during combat
        combatService.clearCombatState(gameData);
        gameData.permanentsToSacrificeAtEndOfCombat.clear();

        // Rule 723.1d: Skip to cleanup step
        gameData.currentStep = TurnStep.CLEANUP;
        gameHelper.resetEndOfTurnModifiers(gameData);
        gameData.priorityPassedBy.clear();

        // Flag so resolveTopOfStack exiles the resolving card instead of graveyard (rule 723.1b)
        gameData.endTurnRequested = true;

        String logEntry = "The turn ends.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - End the turn effect resolved, skipping to cleanup", gameData.id);
    }

    private void resolveAdditionalCombatMainPhase(GameData gameData, StackEntry entry, AdditionalCombatMainPhaseEffect effect) {
        if (effect.count() <= 0) {
            return;
        }

        gameData.additionalCombatMainPhasePairs += effect.count();

        String logEntry = "After this main phase, there is an additional combat phase followed by an additional main phase.";
        if (effect.count() > 1) {
            logEntry = "After this main phase, there are " + effect.count()
                    + " additional combat phases followed by additional main phases.";
        }
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} queued {} additional combat/main phase pair(s)",
                gameData.id, entry.getCard().getName(), effect.count());
    }
}

