package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnSnapshotEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a mass goad effect against the creatures present at resolution time. */
@Component
@RequiredArgsConstructor
public class GoadCreaturesUntilNextTurnSnapshotEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GoadCreaturesUntilNextTurnSnapshotEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GoadCreaturesUntilNextTurnSnapshotEffect goad =
                (GoadCreaturesUntilNextTurnSnapshotEffect) effect;
        FilterContext context = FilterContext.of(gameData)
                .withSourceControllerId(entry.getControllerId());

        gameData.forEachPermanent((ignoredControllerId, permanent) -> {
            if (!gameQueryService.isCreature(gameData, permanent)
                    || !predicateEvaluationService.matchesPermanentPredicate(
                    permanent, goad.affectedPredicate(), context)) {
                return;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard() == null ? "Goad" : entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), goad, permanent.getId(),
                    null, null, EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));
        });
    }
}
