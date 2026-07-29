package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PhaseOutPermanentsEffect}: every matching permanent phases out at once via
 * {@link PhasingService#phaseOut}, which also carries attachments (CR 702.26g) and clears combat
 * state (CR 702.26b).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PhaseOutPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final PhasingService phasingService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PhaseOutPermanentsEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        List<Permanent> toPhaseOut = new ArrayList<>();
        if (e.controllerOnly()) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
            if (battlefield != null) {
                collectMatching(battlefield, e, filterContext, toPhaseOut);
            }
        } else {
            gameData.forEachBattlefield((playerId, battlefield) ->
                    collectMatching(battlefield, e, filterContext, toPhaseOut));
        }

        if (toPhaseOut.isEmpty()) {
            return;
        }

        phasingService.phaseOut(gameData, toPhaseOut);
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(String.format(" phases out %d permanent(s).", toPhaseOut.size()))
                .build());
        log.info("Game {} - {} phases out {} permanents", gameData.id,
                entry.getCard() != null ? entry.getCard().getName() : "effect", toPhaseOut.size());
    }

    private void collectMatching(List<Permanent> battlefield, PhaseOutPermanentsEffect effect,
                                 FilterContext filterContext, List<Permanent> out) {
        for (Permanent permanent : battlefield) {
            if (effect.filter() == null
                    || predicateEvaluationService.matchesPermanentPredicate(permanent, effect.filter(), filterContext)) {
                out.add(permanent);
            }
        }
    }
}
