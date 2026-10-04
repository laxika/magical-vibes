package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeTargetPlayersLifeTotalsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeTargetPlayersLifeTotalsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExchangeTargetPlayersLifeTotalsEffect exchange = (ExchangeTargetPlayersLifeTotalsEffect) effect;
        List<UUID> targets = entry.getTargetIds();
        UUID playerA;
        UUID playerB;
        if (exchange.controllerAndTarget()) {
            playerA = entry.getControllerId();
            playerB = targets.size() == 1 ? targets.getFirst() : entry.getTargetId();
            if (playerA == null || playerB == null) return;
        } else {
            if (targets.size() != 2) return;
            playerA = targets.get(0);
            playerB = targets.get(1);
        }

        // If either player's life total can't change, the exchange doesn't occur.
        if (!gameQueryService.canPlayerLifeChange(gameData, playerA)) {
            String playerName = gameData.playerIdToName.get(playerA);
            gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change. Exchange doesn't occur."));
            return;
        }
        if (!gameQueryService.canPlayerLifeChange(gameData, playerB)) {
            String playerName = gameData.playerIdToName.get(playerB);
            gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change. Exchange doesn't occur."));
            return;
        }

        int lifeA = gameData.getLife(playerA);
        int lifeB = gameData.getLife(playerB);

        if (lifeA == lifeB) {
            String nameA = gameData.playerIdToName.get(playerA);
            String nameB = gameData.playerIdToName.get(playerB);
            gameLogService.append(gameData, GameLog.text(nameA + " and " + nameB + " exchange life totals (both at " + lifeA + ")."));
            return;
        }

        boolean aWouldGain = lifeB > lifeA;
        boolean bWouldGain = lifeA > lifeB;
        boolean aCantGain = aWouldGain && !gameQueryService.canPlayerGainLife(gameData, playerA);
        boolean bCantGain = bWouldGain && !gameQueryService.canPlayerGainLife(gameData, playerB);

        if (aCantGain || bCantGain
                || (lifeB < lifeA && !gameQueryService.canPlayerLoseLife(gameData, playerA))
                || (lifeA < lifeB && !gameQueryService.canPlayerLoseLife(gameData, playerB))) {
            String nameA = gameData.playerIdToName.get(playerA);
            String nameB = gameData.playerIdToName.get(playerB);
            gameLogService.append(gameData, GameLog.text(nameA + " and " + nameB
                    + " can't complete the life-total exchange. Exchange doesn't occur."));
            return;
        }

        String nameA = gameData.playerIdToName.get(playerA);
        String nameB = gameData.playerIdToName.get(playerB);

        lifeSupport.applySetLifeTotal(gameData, playerA, lifeB);
        lifeSupport.applySetLifeTotal(gameData, playerB, lifeA);
        int newLifeA = gameData.getLife(playerA);
        int newLifeB = gameData.getLife(playerB);
        gameLogService.append(gameData, GameLog.text(nameA + " and " + nameB + " exchange life totals (" + nameA + ": " + lifeA + " -> " + newLifeA
                + ", " + nameB + ": " + lifeB + " -> " + newLifeB + ")."));

        log.info("Game {} - {} and {} exchange life totals ({} -> {}, {} -> {})",
                gameData.id, nameA, nameB, lifeA, newLifeA, lifeB, newLifeB);
    }
}
