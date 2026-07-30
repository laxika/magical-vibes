package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackSourcePermanentNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MustAttackSourcePermanentNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final TurnSupport turnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MustAttackSourcePermanentNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = turnSupport.resolveTargetPlayer(gameData, entry);
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (targetPlayerId == null || sourcePermanentId == null) {
            return;
        }

        gameData.tauntedNextTurn.put(targetPlayerId, sourcePermanentId);

        String sourceName = entry.getCard() != null ? entry.getCard().getName() : "it";
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.text("Creatures " + targetName + " controls will attack "
                + sourceName + " if able during their next turn."));
        log.info("Game {} - {} must attack {} next turn", gameData.id, targetName, sourceName);
    }
}
