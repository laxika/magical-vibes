package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PerpetuallyBoostCardEffect}; a change to the cards a subject names is logged as
 * {@link PerpetualChangeLog} says.
 */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyBoostCardEffect boost = (PerpetuallyBoostCardEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), context);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), context);
        if (boost.cards() != null) {
            var located = subjectResolutionService.locatedCards(gameData, entry, boost, boost.cards());
            for (var card : located) {
                PerpetualCardPowerToughnessSupport.remember(gameData, card.card(), powerBoost, toughnessBoost);
            }
            PerpetualChangeLog.log(gameLogService, subjectResolutionService, gameData, entry, boost, boost.cards(),
                    located, String.format(" perpetually gets %+d/%+d.", powerBoost, toughnessBoost));
            return;
        }
        // "This creature perpetually gets ...": the ability's own object, its physical card (a copying permanent's
        // own card), while it is still that object (CR 400.7).
        var self = subjectResolutionService.sourceObjectCard(gameData, entry, effect);
        if (self != null) {
            PerpetualCardPowerToughnessSupport.remember(gameData, self.card(), powerBoost, toughnessBoost);
        }
    }
}
