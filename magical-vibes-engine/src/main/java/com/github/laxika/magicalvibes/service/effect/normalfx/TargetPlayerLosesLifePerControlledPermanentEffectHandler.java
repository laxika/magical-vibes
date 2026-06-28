package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifePerControlledPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerLosesLifePerControlledPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerLosesLifePerControlledPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerLosesLifePerControlledPermanentEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();

        // Count matching permanents the controller controls
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        long count = battlefield == null ? 0 : battlefield.stream()
                .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, e.filter()))
                .count();
        int lossAmount = (int) count * e.multiplier();

        if (lossAmount <= 0) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getCard().getName() + " causes 0 life loss (no matching permanents).");
            return;
        }

        // Target loses life
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
            gameBroadcastService.logAndBroadcast(gameData, targetName + "'s life total can't change.");
        } else {
            int targetCurrentLife = gameData.getLife(targetPlayerId);
            gameData.playerLifeTotals.put(targetPlayerId, targetCurrentLife - lossAmount);

            String lossLog = targetName + " loses " + lossAmount + " life (" + entry.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, lossLog);
            log.info("Game {} - {} loses {} life from {}", gameData.id, targetName, lossAmount, entry.getCard().getName());
        }
    }
}
