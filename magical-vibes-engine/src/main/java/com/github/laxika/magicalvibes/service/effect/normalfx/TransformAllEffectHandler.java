package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TransformAllEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformAllEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AnimationSupport animationSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TransformAllEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TransformAllEffect) effect;
        gameData.forEachPermanent((playerId, perm) -> {
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, perm, e.filter())) {
                return;
            }
            if (gameQueryService.isTransformPrevented(gameData, perm)) {
                log.info("Game {} - {} can't transform (transform prevented)", gameData.id, perm.getCard().getName());
                return;
            }
            if (!perm.isTransformed()) {
                animationSupport.transformToBackFace(gameData, perm);
            } else {
                animationSupport.transformToFrontFace(gameData, perm);
            }
        });
    }
}
