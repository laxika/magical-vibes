package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExactLifeLoseGameThenAdjustLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link ExactLifeLoseGameThenAdjustLifeEffect} (Triskaidekaphobia): players at exactly
 * {@code exactLife} lose simultaneously first; survivors then gain/lose life per {@code lifeDelta}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExactLifeLoseGameThenAdjustLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameOutcomeService gameOutcomeService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExactLifeLoseGameThenAdjustLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExactLifeLoseGameThenAdjustLifeEffect) effect;
        String sourceName = entry.getCard() != null ? entry.getCard().getName() : "ability";

        // Snapshot who is at exactly N — simultaneous check (ruling: both at 13 ⇒ draw).
        List<UUID> atExactLife = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (gameData.getLife(playerId) == e.exactLife()) {
                atExactLife.add(playerId);
            }
        }

        List<UUID> losers = new ArrayList<>();
        for (UUID playerId : atExactLife) {
            if (!gameQueryService.canPlayerLoseGame(gameData, playerId)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.text(gameData.playerIdToName.get(playerId) + " can't lose the game."));
                continue;
            }
            // Lich's Mirror replaces that player's loss; they are not a "loser" for the draw/win check.
            if (gameOutcomeService.replaceLossWithGameReset(gameData, playerId)) {
                continue;
            }
            losers.add(playerId);
        }

        if (!losers.isEmpty()) {
            for (UUID loserId : losers) {
                String loserName = gameData.playerIdToName.get(loserId);
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.textCardText(loserName + " loses the game from ", entry.getCard(), "."));
                log.info("Game {} - {} loses the game from {}", gameData.id, loserName, sourceName);
            }

            // All remaining players lose simultaneously ⇒ draw (CR 104.4a / Triskaidekaphobia ruling).
            if (losers.size() >= gameData.orderedPlayerIds.size()) {
                gameOutcomeService.declareDraw(gameData);
                return;
            }

            // Exactly one loser in a 2-player game ⇒ the other wins; skip life adjustment.
            if (losers.size() == 1) {
                UUID winnerId = gameQueryService.getOpponentId(gameData, losers.getFirst());
                gameOutcomeService.declareWinner(gameData, winnerId);
                return;
            }

            // Multiplayer edge: more than one but not all — remaining player(s) win collectively;
            // this engine is 2-player, so this branch should not fire.
            UUID survivor = gameData.orderedPlayerIds.stream()
                    .filter(id -> !losers.contains(id))
                    .findFirst()
                    .orElse(null);
            if (survivor != null) {
                gameOutcomeService.declareWinner(gameData, survivor);
            }
            return;
        }

        if (gameData.status == GameStatus.FINISHED) {
            return;
        }

        // Nobody lost — each player gains or loses life.
        int delta = e.lifeDelta();
        if (delta == 0) {
            return;
        }
        if (delta > 0) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                lifeSupport.applyGainLife(gameData, playerId, delta, sourceName);
            }
        } else {
            int loss = -delta;
            for (UUID playerId : gameData.orderedPlayerIds) {
                lifeSupport.applyLifeLoss(gameData, playerId, loss, sourceName);
            }
        }
    }
}
