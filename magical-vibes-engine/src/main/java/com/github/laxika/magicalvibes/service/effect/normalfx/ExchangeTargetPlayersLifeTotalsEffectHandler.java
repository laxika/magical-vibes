package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeTargetPlayersLifeTotalsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeTargetPlayersLifeTotalsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeTargetPlayersLifeTotalsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getTargetIds();
        if (targets.size() != 2) return;

        UUID playerA = targets.get(0);
        UUID playerB = targets.get(1);

        // CR 118.7: If either player's life total can't change, the exchange doesn't occur
        if (!gameQueryService.canPlayerLifeChange(gameData, playerA)) {
            String playerName = gameData.playerIdToName.get(playerA);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s life total can't change. Exchange doesn't occur."));
            return;
        }
        if (!gameQueryService.canPlayerLifeChange(gameData, playerB)) {
            String playerName = gameData.playerIdToName.get(playerB);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s life total can't change. Exchange doesn't occur."));
            return;
        }

        int lifeA = gameData.getLife(playerA);
        int lifeB = gameData.getLife(playerB);

        if (lifeA == lifeB) {
            String nameA = gameData.playerIdToName.get(playerA);
            String nameB = gameData.playerIdToName.get(playerB);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(nameA + " and " + nameB + " exchange life totals (both at " + lifeA + ")."));
            return;
        }

        // Per CR 119.7e: An exchange is implemented as each player gaining or losing life.
        // If a player can't gain life, they can't move to a higher life total via exchange.
        boolean aWouldGain = lifeB > lifeA;
        boolean bWouldGain = lifeA > lifeB;
        boolean aCantGain = aWouldGain && !gameQueryService.canPlayerGainLife(gameData, playerA);
        boolean bCantGain = bWouldGain && !gameQueryService.canPlayerGainLife(gameData, playerB);

        if (aCantGain && bCantGain) {
            String nameA = gameData.playerIdToName.get(playerA);
            String nameB = gameData.playerIdToName.get(playerB);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(nameA + " and " + nameB + " can't gain life. Exchange doesn't occur."));
            return;
        }

        String nameA = gameData.playerIdToName.get(playerA);
        String nameB = gameData.playerIdToName.get(playerB);

        int newLifeA = aCantGain ? lifeA : lifeB;
        int newLifeB = bCantGain ? lifeB : lifeA;

        if (aCantGain) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(nameA + " can't gain life."));
        }
        if (bCantGain) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(nameB + " can't gain life."));
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(nameA + " and " + nameB + " exchange life totals (" + nameA + ": " + lifeA + " -> " + newLifeA
                        + ", " + nameB + ": " + lifeB + " -> " + newLifeB + ")."));

        // Apply the new totals with triggers (bypass applySetLifeTotal since we already checked)
        gameData.playerLifeTotals.put(playerA, newLifeA);
        gameData.playerLifeTotals.put(playerB, newLifeB);

        if (newLifeA > lifeA) {
            triggerCollectionService.checkLifeGainTriggers(gameData, playerA, newLifeA - lifeA);
        } else if (newLifeA < lifeA) {
            triggerCollectionService.checkLifeLossTriggers(gameData, playerA, lifeA - newLifeA);
        }
        if (newLifeB > lifeB) {
            triggerCollectionService.checkLifeGainTriggers(gameData, playerB, newLifeB - lifeB);
        } else if (newLifeB < lifeB) {
            triggerCollectionService.checkLifeLossTriggers(gameData, playerB, lifeB - newLifeB);
        }

        log.info("Game {} - {} and {} exchange life totals ({} -> {}, {} -> {})",
                gameData.id, nameA, nameB, lifeA, newLifeA, lifeB, newLifeB);
    }
}
