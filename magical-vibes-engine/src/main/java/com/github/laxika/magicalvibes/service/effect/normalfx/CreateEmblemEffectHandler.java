package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
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

        for (UUID recipientId : recipients(gameData, entry, emblemEffect)) {
            if (!gameData.playerIds.contains(recipientId)) {
                continue;
            }
            String playerName = gameData.playerIdToName.get(recipientId);

            gameData.emblems.add(new Emblem(recipientId, emblemEffect.staticEffects(), entry.getCard()));

            gameLogService.append(gameData, GameLog.text(
                    playerName + " gets an emblem with \"" + emblemEffect.reminderText() + "\"."));
            log.info("Game {} - {} gets an emblem from {}", gameData.id, playerName, entry.getCard().getName());
        }
    }

    private static List<UUID> recipients(GameData gameData, StackEntry entry,
                                         CreateEmblemEffect emblemEffect) {
        return switch (emblemEffect.recipient()) {
            case TARGET_PLAYER -> List.of(entry.getTargetId());
            case CONTROLLER -> List.of(entry.getControllerId());
            case EACH_OPPONENT -> gameData.orderedPlayerIds.stream()
                    .filter(playerId -> !playerId.equals(entry.getControllerId()))
                    .toList();
            // "Each player dealt damage this way": the damage effect earlier in this same entry logged
            // exactly who took damage, so fully-prevented players are skipped.
            case EACH_PLAYER_DEALT_DAMAGE_THIS_WAY -> List.copyOf(entry.getPlayersDealtDamageThisResolution());
        };
    }
}
