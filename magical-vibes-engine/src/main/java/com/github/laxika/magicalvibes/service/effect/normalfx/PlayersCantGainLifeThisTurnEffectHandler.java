package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayersCantGainLifeThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayersCantGainLifeThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var restriction = (PlayersCantGainLifeThisTurnEffect) effect;
        if (restriction.players().equals(Subjects.eachPlayer())) {
            // One effect on every player (no affected player named).
            restrict(gameData, entry, effect, null);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), ": players can't gain life this turn."));
            return;
        }

        List<UUID> players = subjectResolutionService.players(gameData, entry, effect, restriction.players());
        players.forEach(playerId -> restrict(gameData, entry, effect, playerId));
        if (restriction.players().equals(Subjects.eachOpponent())) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), ": opponents can't gain life this turn."));
            return;
        }
        for (UUID playerId : players) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    ": " + gameData.playerIdToName.get(playerId) + " can't gain life this turn."));
        }
    }

    private static void restrict(GameData gameData, StackEntry entry, CardEffect effect, UUID playerId) {
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), effect, playerId,
                Duration.UNTIL_END_OF_TURN));
    }
}
