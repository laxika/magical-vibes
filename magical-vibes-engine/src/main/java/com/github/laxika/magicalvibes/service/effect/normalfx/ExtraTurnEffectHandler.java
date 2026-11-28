package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExtraTurnEffectHandler implements NormalEffectHandlerBean {

    private final TurnSupport turnSupport;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExtraTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExtraTurnEffect) effect;
        if (e.player().declaresTarget()) {
            UUID targetPlayerId = turnSupport.resolveTargetPlayer(gameData, entry);
            if (targetPlayerId != null) {
                grantExtraTurns(gameData, targetPlayerId, e.count());
            }
            return;
        }
        List<UUID> players = subjectResolutionService.players(gameData, entry, effect, e.player());
        for (UUID playerId : players) {
            grantExtraTurns(gameData, playerId, e.count());
        }
    }

    private void grantExtraTurns(GameData gameData, UUID playerId, int count) {
        String playerName = gameData.playerIdToName.get(playerId);
        for (int i = 0; i < count; i++) {
            gameData.queueExtraTurnFirst(playerId, false);
        }

        String logEntry = playerName + " takes " + count + " extra "
                + TurnSupport.pluralize("turn", count) + " after this one.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} granted {} extra turn(s)", gameData.id, playerName, count);
    }
}
