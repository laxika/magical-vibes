package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Registers Urabrask, Heretic Praetor's next-draw replacement for the active opponent. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterNextDrawExileTopCardMayPlayThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterNextDrawExileTopCardMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID affectedPlayerId = entry.getTargetId();
        if (affectedPlayerId == null) {
            return;
        }

        gameData.pendingNextDrawExileTopCard.merge(affectedPlayerId, 1, Integer::sum);

        String playerName = gameData.playerIdToName.get(affectedPlayerId);
        gameLogService.append(gameData, GameLog.builder()
                .text("The next time " + playerName + " would draw a card this turn, they will exile the top card of their library instead (")
                .card(entry.getCard())
                .text(").")
                .build());
        log.info("Game {} - {} registers a next-draw exile replacement", gameData.id, playerName);
    }
}
