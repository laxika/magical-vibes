package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapUpToControlledPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UntapUpToControlledPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UntapUpToControlledPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (UntapUpToControlledPermanentsEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        int remaining = e.count();
        int count = 0;
        for (Permanent p : battlefield) {
            if (remaining <= 0) break;
            if (!p.isTapped()) continue;
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter())) continue;

            tapUntapSupport.untapPermanent(gameData, p);
            count++;
            remaining--;
        }

        if (count > 0) {
            String logEntry = entry.getCard().getName() + " untaps " + count + " permanent(s).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        }
        log.info("Game {} - {} untaps {} permanent(s) via delayed trigger", gameData.id, entry.getCard().getName(), count);
    }
}
