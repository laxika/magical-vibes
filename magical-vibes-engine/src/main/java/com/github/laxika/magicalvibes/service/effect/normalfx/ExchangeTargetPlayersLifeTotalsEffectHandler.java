package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeTargetPlayersLifeTotalsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Exchanges two players' life totals.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: the exchange is its first change, and each player gains or loses the
 * life needed to reach the other's former total (CR 701.12c), so a Lich's Duel Mastery the losing player's loss would
 * sacrifice instead is asked about (CR 616.1) before either life total changes: the gaining player's change would
 * otherwise close the question window first.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeTargetPlayersLifeTotalsEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

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

        Map<UUID, Integer> newLifeTotals = new LinkedHashMap<>();
        newLifeTotals.put(playerA, lifeB);
        newLifeTotals.put(playerB, lifeA);
        lifeSupport.askBeforeLifeTotalsChange(gameData, newLifeTotals);
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
