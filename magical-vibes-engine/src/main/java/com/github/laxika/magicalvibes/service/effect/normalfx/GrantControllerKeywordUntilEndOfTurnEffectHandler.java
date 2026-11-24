package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Floats the player keyword on each player the effect names, for its duration, one log line per player. */
@Component
@RequiredArgsConstructor
public class GrantControllerKeywordUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantControllerKeywordUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantControllerKeywordUntilEndOfTurnEffect) effect;
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, grant.players())) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), grant, playerId, grant.duration()));
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                    + " gains " + grant.keyword().name().toLowerCase()
                    + (Duration.UNTIL_YOUR_NEXT_TURN.equals(grant.duration())
                    ? " until their next turn." : " until end of turn.")));
        }
    }
}
