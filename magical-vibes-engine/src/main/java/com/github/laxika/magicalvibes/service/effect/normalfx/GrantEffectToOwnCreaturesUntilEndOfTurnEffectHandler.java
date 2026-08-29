package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantEffectToOwnCreaturesUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEffectToOwnCreaturesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantEffectToOwnCreaturesUntilEndOfTurnEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        int count = 0;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                if (e.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, e.filter())) {
                    continue;
                }
                permanent.addTemporaryTriggeredEffect(e.slot(), e.grantedEffect());
                count++;
            }
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" grants a temporary " + e.slot().name() + " ability to " + count
                        + " creature(s) until end of turn.")
                .build());
        log.info("Game {} - {} grants temporary {} effect to {} own creature(s)",
                gameData.id, entry.getCard().getName(), e.slot().name(), count);
    }
}
