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

    private enum DeathReason {
        ZERO_TOUGHNESS, LETHAL_DAMAGE, ZERO_LOYALTY
    }

    private record DeathEntry(Permanent permanent, DeathReason reason) {}

    public void performStateBasedActions(GameData gameData) {
        destroyLethalCreaturesAndPlaneswalkers(gameData);

        // CR 704.5a — player with 0 or less life loses the game
        // CR 704.5c — player with ten or more poison counters loses the game
        if (gameOutcomeService.checkWinCondition(gameData)) {
            return;
        }

        sacrificeCompletedSagas(gameData);
        cancelCounters(gameData);

        // CR 603.8 — check state-triggered abilities after SBAs
        stateTriggerService.checkStateTriggers(gameData);

        checkEmptyLibraryLoss(gameData);
    }

    private void destroyLethalCreaturesAndPlaneswalkers(GameData gameData) {
        List<DeathEntry> toDie = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (gameQueryService.isCreature(gameData, p) && gameQueryService.getEffectiveToughness(gameData, p) <= 0) {
                toDie.add(new DeathEntry(p, DeathReason.ZERO_TOUGHNESS));
            } else if (gameQueryService.isCreature(gameData, p)
                    && p.getMarkedDamage() >= gameQueryService.getEffectiveToughness(gameData, p)
                    && !gameQueryService.hasKeyword(gameData, p, Keyword.INDESTRUCTIBLE)
                    && !graveyardService.tryRegenerate(gameData, p)) {
                // CR 704.5g — creature with damage >= toughness is destroyed (regeneration can replace this)
                toDie.add(new DeathEntry(p, DeathReason.LETHAL_DAMAGE));
            } else if (p.getCard().hasType(CardType.PLANESWALKER) && p.getLoyaltyCounters() <= 0) {
                toDie.add(new DeathEntry(p, DeathReason.ZERO_LOYALTY));
            }
        });

        for (DeathEntry entry : toDie) {
            permanentRemovalService.removePermanentToGraveyard(gameData, entry.permanent());
            String name = entry.permanent().getCard().getName();
            switch (entry.reason()) {
                case ZERO_TOUGHNESS -> {
                    gameBroadcastService.logAndBroadcast(gameData, name + " is put into the graveyard (0 toughness).");
                    log.info("Game {} - {} dies to state-based actions (0 toughness)", gameData.id, name);
                }
                case LETHAL_DAMAGE -> {
                    gameBroadcastService.logAndBroadcast(gameData, name + " is destroyed (lethal damage).");
                    log.info("Game {} - {} dies to state-based actions (lethal damage)", gameData.id, name);
                }
                case ZERO_LOYALTY -> {
                    gameBroadcastService.logAndBroadcast(gameData, name + " has no loyalty counters and is put into the graveyard.");
                    log.info("Game {} - {} dies to state-based actions (0 loyalty)", gameData.id, name);
                }
            }
        }

        if (!toDie.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    // CR 714.4 — Saga with lore counters >= final chapter is sacrificed
    // (unless it has a chapter ability still on the stack)
    private void sacrificeCompletedSagas(GameData gameData) {
        List<Permanent> sagasToSacrifice = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (!p.getCard().isSaga()) return;
            int finalChapter = p.getCard().getSagaFinalChapter();
            if (finalChapter <= 0 || p.getLoreCounters() < finalChapter) return;

            boolean chapterOnStack = gameData.stack.stream()
                    .anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                            && p.getId().equals(e.getSourcePermanentId()));
            if (!chapterOnStack) {
                sagasToSacrifice.add(p);
            }
        });

        for (Permanent saga : sagasToSacrifice) {
            permanentRemovalService.removePermanentToGraveyard(gameData, saga);
            String logEntry = saga.getCard().getName() + " is sacrificed (final chapter reached).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} sacrificed (lore counters >= final chapter)", gameData.id, saga.getCard().getName());
        }
    }

    // CR 704.5q — +1/+1 and -1/-1 counters cancel each other out
    private void cancelCounters(GameData gameData) {
        gameData.forEachPermanent((pid, p) -> {
            int plus = p.getPlusOnePlusOneCounters();
            int minus = p.getMinusOneMinusOneCounters();
            if (plus > 0 && minus > 0) {
                int cancelled = Math.min(plus, minus);
                p.setPlusOnePlusOneCounters(plus - cancelled);
                p.setMinusOneMinusOneCounters(minus - cancelled);
                // Protean Hydra ruling: "its last ability triggers whenever a +1/+1 counter is removed
                // from it for any reason" — SBA counter annihilation triggers regrowth
                if (p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof DelayedPlusOnePlusOneCounterRegrowthEffect)) {
                    int pending = gameData.pendingDelayedPlusOnePlusOneCounters.getOrDefault(p.getId(), 0);
                    gameData.pendingDelayedPlusOnePlusOneCounters.put(p.getId(), pending + cancelled * 2);
                }
            }
        });
    }

    // CR 704.5b — player who attempted to draw from an empty library loses the game
    private void checkEmptyLibraryLoss(GameData gameData) {
        if (gameData.playersAttemptedDrawFromEmptyLibrary.isEmpty()) return;

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
