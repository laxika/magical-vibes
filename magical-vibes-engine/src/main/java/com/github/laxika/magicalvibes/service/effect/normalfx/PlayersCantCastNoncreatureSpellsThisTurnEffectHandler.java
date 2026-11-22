package com.github.laxika.magicalvibes.service.effect.normalfx;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastNoncreatureSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayersCantCastNoncreatureSpellsThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayersCantCastNoncreatureSpellsThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var restriction = (PlayersCantCastNoncreatureSpellsThisTurnEffect) effect;
        if (restriction.players().equals(Subjects.eachPlayer())) {
            // One effect on every player (no affected player named).
            restrict(gameData, entry, effect, null);
            gameLogService.append(gameData, GameLog.text("Players can't cast noncreature spells this turn."));
            return;
        }
        subjectResolutionService.players(gameData, entry, effect, restriction.players())
                .forEach(playerId -> restrict(gameData, entry, effect, playerId));
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                + "'s opponents can't cast noncreature spells this turn."));
    }

    private void restrict(GameData gameData, StackEntry entry, CardEffect effect, UUID playerId) {
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), effect, playerId, Duration.UNTIL_END_OF_TURN));
    }
}
