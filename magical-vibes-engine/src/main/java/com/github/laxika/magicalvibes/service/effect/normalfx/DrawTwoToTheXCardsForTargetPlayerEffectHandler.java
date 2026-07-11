package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawTwoToTheXCardsForTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrawTwoToTheXCardsForTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawTwoToTheXCardsForTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        // 2^X cards. Clamp the exponent so the shift can't overflow for absurd X values.
        int exponent = Math.max(0, Math.min(entry.getXValue(), 30));
        int amount = 1 << exponent;

        for (int i = 0; i < amount; i++) {
            drawService.resolveDrawCard(gameData, targetPlayerId);
        }

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = playerName + " draws " + amount + " card" + (amount != 1 ? "s" : "")
                + " (" + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} draws {} from {}", gameData.id, playerName, amount, entry.getCard().getName());
    }
}
