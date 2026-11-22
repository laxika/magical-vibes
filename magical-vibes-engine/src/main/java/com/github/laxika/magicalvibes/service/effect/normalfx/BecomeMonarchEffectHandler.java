package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BecomeMonarchEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeMonarchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BecomeMonarchEffect becomes = (BecomeMonarchEffect) effect;
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, becomes.player())) {
            if (playerId.equals(gameData.monarchPlayerId)) {
                continue;
            }
            gameData.monarchPlayerId = playerId;
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.getOrDefault(playerId, "A player") + " becomes the monarch."));
            permanentRemovalService.returnExileReturnsOnOpponentBecomesMonarch(gameData, playerId);
            ActionEvents.becameMonarch(gameData, playerId);
            return;
        }
    }
}
