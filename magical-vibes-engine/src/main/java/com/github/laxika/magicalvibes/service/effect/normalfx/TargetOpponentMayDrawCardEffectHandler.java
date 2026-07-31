package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentMayDrawCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetOpponentMayDrawCardEffect}: queues the optional draw for the resolving
 * controller's opponent, so that player is the one asked. Mirrors how
 * {@code DefendingPlayerMayDrawCardEffect} routes Sibilant Spirit's draw to the defending player.
 */
@Component
@RequiredArgsConstructor
public class TargetOpponentMayDrawCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetOpponentMayDrawCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
        if (opponentId == null) {
            return;
        }

        gameData.queueMayAbility(entry.getCard(), opponentId, new MayEffect(new DrawCardEffect(), "Draw a card?"));
        gameLogService.append(gameData, GameLog.textCardText(
                "", entry.getCard(), " offers " + gameData.playerIdToName.get(opponentId) + " a card."));
    }
}
