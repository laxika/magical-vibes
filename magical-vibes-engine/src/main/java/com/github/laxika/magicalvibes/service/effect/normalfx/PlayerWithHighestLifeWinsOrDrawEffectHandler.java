package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerWithHighestLifeWinsOrDrawEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlayerWithHighestLifeWinsOrDrawEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayerWithHighestLifeWinsOrDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int highestLife = gameData.orderedPlayerIds.stream()
                .mapToInt(gameData::getLife)
                .max()
                .orElse(0);
        List<UUID> highestLifePlayers = gameData.orderedPlayerIds.stream()
                .filter(playerId -> gameData.getLife(playerId) == highestLife)
                .toList();

        if (highestLifePlayers.size() != 1) {
            gameOutcomeService.declareDraw(gameData);
            return;
        }

        UUID winnerId = highestLifePlayers.getFirst();
        if (!gameOutcomeService.canPlayerWinGame(gameData, winnerId)) {
            String winnerName = gameData.playerIdToName.get(winnerId);
            gameLogService.append(gameData, GameLog.builder()
                    .card(entry.getCard())
                    .text(winnerName + "'s win condition is met but the game cannot be won.")
                    .build());
            return;
        }

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(winnerId) + " wins the game from ", entry.getCard(), "!"));
        gameOutcomeService.declareWinner(gameData, winnerId);
    }
}
