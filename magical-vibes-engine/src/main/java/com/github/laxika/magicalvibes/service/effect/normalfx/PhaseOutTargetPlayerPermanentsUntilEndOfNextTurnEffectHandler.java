package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutTargetPlayerPermanentsUntilEndOfNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves a targeted mass phase-out held until the end of the resolving player's next turn. */
@Component
@RequiredArgsConstructor
public class PhaseOutTargetPlayerPermanentsUntilEndOfNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final PhasingService phasingService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutTargetPlayerPermanentsUntilEndOfNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() == null) {
            return;
        }

        var phaseOutEffect = (PhaseOutTargetPlayerPermanentsUntilEndOfNextTurnEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getTargetId());
        if (battlefield == null) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());
        List<Permanent> matching = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (phaseOutEffect.filter() == null
                    || predicateEvaluationService.matchesPermanentPredicate(
                    permanent, phaseOutEffect.filter(), filterContext)) {
                matching.add(permanent);
            }
        }
        phasingService.phaseOutUntilEndOfNextTurn(gameData, matching, entry.getControllerId());
    }
}
