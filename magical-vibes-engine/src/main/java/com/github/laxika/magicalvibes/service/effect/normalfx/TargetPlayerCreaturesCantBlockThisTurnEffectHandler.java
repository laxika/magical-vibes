package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCreaturesCantBlockThisTurnEffect;
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
public class TargetPlayerCreaturesCantBlockThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerCreaturesCantBlockThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        // Determine the affected player: if target is a player, use directly;
        // if target is a planeswalker, use its controller
        UUID affectedPlayerId;
        if (gameData.playerIds.contains(targetId)) {
            affectedPlayerId = targetId;
        } else {
            affectedPlayerId = gameQueryService.findPermanentController(gameData, targetId);
            if (affectedPlayerId == null) return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(affectedPlayerId);
        if (battlefield == null) return;

        String playerName = gameData.playerIdToName.get(affectedPlayerId);
        int count = 0;
        for (Permanent p : battlefield) {
            if (gameQueryService.isCreature(gameData, p)) {
                p.setCantBlockThisTurn(true);
                count++;
            }
        }

        if (count > 0) {
            String logEntry = "Creatures controlled by " + playerName + " can't block this turn.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} creatures controlled by {} can't block this turn", gameData.id, count, playerName);
        }
    }
}
