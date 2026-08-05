package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemRecipient;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adds the emblem to {@code gameData.emblems} under the recipient's control. The effect's static
 * effects are stored verbatim; nothing here resolves them.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateEmblemEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateEmblemEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateEmblemEffect emblemEffect = (CreateEmblemEffect) effect;

        UUID recipientId = emblemEffect.recipient() == EmblemRecipient.TARGET_PLAYER
                ? entry.getTargetId()
                : entry.getControllerId();
        if (!gameData.playerIds.contains(recipientId)) {
            return;
        }
        String playerName = gameData.playerIdToName.get(recipientId);

        gameData.emblems.add(new Emblem(recipientId, emblemEffect.staticEffects(), entry.getCard()));

        gameLogService.append(gameData, GameLog.text(
                playerName + " gets an emblem with \"" + emblemEffect.reminderText() + "\"."));
        log.info("Game {} - {} gets an emblem from {}", gameData.id, playerName, entry.getCard().getName());
    }
}
