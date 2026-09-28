package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEachControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExploreEachControlledCreatureEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    public ExploreEachControlledCreatureEffectHandler(PredicateEvaluationService predicateEvaluationService) {
        this.predicateEvaluationService = predicateEvaluationService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExploreEachControlledCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExploreEachControlledCreatureEffect exploreEach = (ExploreEachControlledCreatureEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(
                entry.getControllerId(), List.of());
        FilterContext context = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        List<CardEffect> explores = new ArrayList<>();
        for (Permanent permanent : List.copyOf(battlefield)) {
            if (predicateEvaluationService.matchesPermanentPredicate(permanent, exploreEach.predicate(), context)) {
                explores.add(ExploreEffect.forPermanent(permanent.getId()));
            }
        }

        if (!explores.isEmpty()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, explores);
        }
    }
}
