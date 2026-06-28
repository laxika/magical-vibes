package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExtraTurnEffectHandler implements NormalEffectHandlerBean {

    private final TurnSupport turnSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExtraTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExtraTurnEffect) effect;
        UUID targetPlayerId = turnSupport.resolveTargetPlayer(gameData, entry);
        if (targetPlayerId == null) {
            return;
        }

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        for (int i = 0; i < e.count(); i++) {
            gameData.extraTurns.addFirst(targetPlayerId);
        }

        String logEntry = playerName + " takes " + e.count() + " extra "
                + TurnSupport.pluralize("turn", e.count()) + " after this one.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} granted {} extra turn(s)", gameData.id, playerName, e.count());
    }
}
