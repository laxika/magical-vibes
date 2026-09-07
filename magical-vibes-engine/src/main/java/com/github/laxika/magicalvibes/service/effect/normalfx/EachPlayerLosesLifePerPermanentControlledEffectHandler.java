package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesLifePerPermanentControlledEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerLosesLifePerPermanentControlledEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerLosesLifePerPermanentControlledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerLosesLifePerPermanentControlledEffect) effect;
        gameData.forEachBattlefield((playerId, battlefield) -> {
            int matchingPermanentCount = 0;
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, e.filter())) {
                    matchingPermanentCount++;
                }
            }

            int lifeLoss = matchingPermanentCount * e.lifePerPermanent();
            if (lifeLoss > 0) {
                lifeSupport.applyLifeLoss(gameData, playerId, lifeLoss, entry.getCard().getName());
            }
        });
    }
}
