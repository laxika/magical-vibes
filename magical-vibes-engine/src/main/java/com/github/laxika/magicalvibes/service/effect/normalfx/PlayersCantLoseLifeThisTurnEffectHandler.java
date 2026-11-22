package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PlayersCantLoseLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlayersCantLoseLifeThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayersCantLoseLifeThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var restriction = (PlayersCantLoseLifeThisTurnEffect) effect;
        if (restriction.players().equals(Subjects.eachPlayer())) {
            // One effect on every player (no affected player named).
            restrict(gameData, entry, effect, null);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), ": players can't lose life this turn."));
            return;
        }
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, restriction.players())) {
            restrict(gameData, entry, effect, playerId);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    ": " + gameData.playerIdToName.get(playerId) + " can't lose life this turn."));
        }
    }

    private void restrict(GameData gameData, StackEntry entry, CardEffect effect, UUID playerId) {
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), effect, playerId, Duration.UNTIL_END_OF_TURN));
    }
}
