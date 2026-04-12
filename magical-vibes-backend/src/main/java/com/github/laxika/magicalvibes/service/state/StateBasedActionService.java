package com.github.laxika.magicalvibes.service.state;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.StateTriggerService;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DelayedPlusOnePlusOneCounterRegrowthEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateBasedActionService {

    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardService graveyardService;
    private final StateTriggerService stateTriggerService;

    public void performStateBasedActions(GameData gameData) {
        boolean anyDied = false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            // Two-pass: collect dead permanents first, then remove via PermanentRemovalService
            List<Permanent> toDie = new ArrayList<>();
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p) && gameQueryService.getEffectiveToughness(gameData, p) <= 0) {
                    toDie.add(p);
                } else if (gameQueryService.isCreature(gameData, p)
                        && p.getMarkedDamage() >= gameQueryService.getEffectiveToughness(gameData, p)
                        && !gameQueryService.hasKeyword(gameData, p, Keyword.INDESTRUCTIBLE)
                        && !graveyardService.tryRegenerate(gameData, p)) {
                    // CR 704.5g — creature with damage >= toughness is destroyed (regeneration can replace this)
                    toDie.add(p);
                } else if (p.getCard().hasType(CardType.PLANESWALKER) && p.getLoyaltyCounters() <= 0) {
                    toDie.add(p);
                }
            }

            for (Permanent p : toDie) {
                boolean isCreature = gameQueryService.isCreature(gameData, p);
                permanentRemovalService.removePermanentToGraveyard(gameData, p);

                if (isCreature && gameQueryService.getEffectiveToughness(gameData, p) <= 0) {
                    String logEntry = p.getCard().getName() + " is put into the graveyard (0 toughness).";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} dies to state-based actions (0 toughness)", gameData.id, p.getCard().getName());
                } else if (isCreature) {
                    String logEntry = p.getCard().getName() + " is destroyed (lethal damage).";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} dies to state-based actions (lethal damage)", gameData.id, p.getCard().getName());
                } else {
                    String logEntry = p.getCard().getName() + " has no loyalty counters and is put into the graveyard.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} dies to state-based actions (0 loyalty)", gameData.id, p.getCard().getName());
                }
                anyDied = true;
            }
        }
        if (anyDied) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }

        // CR 704.5a — player with 0 or less life loses the game
        // CR 704.5c — player with ten or more poison counters loses the game
        if (gameOutcomeService.checkWinCondition(gameData)) {
            return;
        }

        // CR 714.4 — Saga with lore counters >= final chapter is sacrificed
        // (unless it has a chapter ability still on the stack)
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            List<Permanent> sagasToSacrifice = new ArrayList<>();
            for (Permanent p : battlefield) {
                if (!p.getCard().isSaga()) continue;
                int finalChapter = p.getCard().getSagaFinalChapter();
                if (finalChapter <= 0 || p.getLoreCounters() < finalChapter) continue;

                // Don't sacrifice if a chapter ability from this Saga is still on the stack (CR 714.4)
                boolean chapterOnStack = gameData.stack.stream()
                        .anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                                && p.getId().equals(e.getSourcePermanentId()));
                if (!chapterOnStack) {
                    sagasToSacrifice.add(p);
                }
            }

            for (Permanent saga : sagasToSacrifice) {
                permanentRemovalService.removePermanentToGraveyard(gameData, saga);
                String logEntry = saga.getCard().getName() + " is sacrificed (final chapter reached).";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sacrificed (lore counters >= final chapter)", gameData.id, saga.getCard().getName());
            }
        }

        // CR 704.5q — +1/+1 and -1/-1 counters cancel each other out
        gameData.forEachPermanent((pid, p) -> {
            int plus = p.getPlusOnePlusOneCounters();
            int minus = p.getMinusOneMinusOneCounters();
            if (plus > 0 && minus > 0) {
                int cancelled = Math.min(plus, minus);
                p.setPlusOnePlusOneCounters(plus - cancelled);
                p.setMinusOneMinusOneCounters(minus - cancelled);
                // Protean Hydra ruling: "its last ability triggers whenever a +1/+1 counter is removed
                // from it for any reason" — SBA counter annihilation triggers regrowth
                if (cancelled > 0 && p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof DelayedPlusOnePlusOneCounterRegrowthEffect)) {
                    int pending = gameData.pendingDelayedPlusOnePlusOneCounters.getOrDefault(p.getId(), 0);
                    gameData.pendingDelayedPlusOnePlusOneCounters.put(p.getId(), pending + cancelled * 2);
                }
            }
        });

        // CR 603.8 — check state-triggered abilities after SBAs
        stateTriggerService.checkStateTriggers(gameData);

        // CR 704.5b — player who attempted to draw from an empty library loses the game
        if (!gameData.playersAttemptedDrawFromEmptyLibrary.isEmpty()) {
            for (UUID playerId : List.copyOf(gameData.playersAttemptedDrawFromEmptyLibrary)) {
                if (gameQueryService.canPlayerLoseGame(gameData, playerId)) {
                    UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                    String logEntry = gameData.playerIdToName.get(playerId) + " attempted to draw from an empty library and loses the game.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} loses (drew from empty library)", gameData.id, gameData.playerIdToName.get(playerId));
                    gameOutcomeService.declareWinner(gameData, winnerId);
                }
            }
            gameData.playersAttemptedDrawFromEmptyLibrary.clear();
        }
    }
}
