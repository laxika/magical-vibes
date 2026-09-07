package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UnsuspectAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.stereotype.Component;

@Component
public class UnsuspectAllCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    public UnsuspectAllCreaturesEffectHandler(GameQueryService gameQueryService,
                                              PredicateEvaluationService predicateEvaluationService) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnsuspectAllCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var unsuspect = (UnsuspectAllCreaturesEffect) effect;
        var filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isSuspected()
                    && gameQueryService.isCreature(gameData, permanent)
                    && (unsuspect.filter() == null
                    || predicateEvaluationService.matchesPermanentPredicate(permanent, unsuspect.filter(), filterContext))) {
                permanent.setSuspected(false);
            }
        });
    }
}
