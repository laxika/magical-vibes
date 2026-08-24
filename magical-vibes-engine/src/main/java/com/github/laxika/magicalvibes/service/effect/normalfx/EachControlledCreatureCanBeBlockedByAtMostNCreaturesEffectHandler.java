package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var restriction = (EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                    new CanBeBlockedByAtMostNCreaturesEffect(restriction.maxBlockers()),
                    permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
            count++;
        }

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " limits the number of creatures that can block " + count
                        + " creature(s) to " + restriction.maxBlockers() + " until end of turn."));
        log.info("Game {} - {} limits blockers for {} creature(s) to {} until end of turn",
                gameData.id, entry.getCard().getName(), count, restriction.maxBlockers());
    }
}
