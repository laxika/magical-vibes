package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEqualToCardsDrawnThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealDamageToEachOpponentEqualToCardsDrawnThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachOpponentEqualToCardsDrawnThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;

            int cardsDrawn = gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0);
            if (cardsDrawn <= 0) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " has drawn no cards this turn — no damage from " + cardName + "."));
                log.info("Game {} - {} drawn 0 cards this turn, no damage from {}",
                        gameData.id, playerName, cardName);
                continue;
            }

            if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(cardName + "'s damage to " + playerName + " is prevented."));
            } else {
                int rawDamage = gameQueryService.applyDamageMultiplier(gameData, cardsDrawn, entry);
                damageSupport.dealDamageToPlayer(gameData, entry, playerId, rawDamage);
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
