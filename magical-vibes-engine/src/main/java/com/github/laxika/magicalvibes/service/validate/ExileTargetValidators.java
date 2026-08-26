package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromGraveyardOrExileToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromExileToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Escape-hatch validator for the exile family. The structural "exile target permanent / creature"
 * effects now carry a harmful {@code TargetSpec} interpreted by {@code TargetValidationService}
 * (PERMANENT / CREATURE, honouring protection); only the exile-zone return below retains a
 * validator, because it validates a card in the EXILE zone (a no-op category in the interpreter)
 * and applies the effect's own card filter.
 */
@Service
@RequiredArgsConstructor
public class ExileTargetValidators {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @ValidatesTarget(ReturnTargetCardFromExileToHandEffect.class)
    public void validateReturnTargetCardFromExile(TargetValidationContext ctx, ReturnTargetCardFromExileToHandEffect effect) {
        if (ctx.targetZone() != Zone.EXILE) {
            throw new IllegalStateException("Effect requires an exile target");
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Effect requires a target card");
        }
        Card exiledCard = gameQueryService.findCardInExileById(ctx.gameData(), ctx.targetId());
        if (exiledCard == null) {
            throw new IllegalStateException("Target card not found in exile");
        }
        if (effect.filter() != null && !predicateEvaluationService.matchesCardPredicate(exiledCard, effect.filter(), null)) {
            String label = CardPredicateUtils.describeFilter(effect.filter());
            throw new IllegalStateException("Target card must be a " + label);
        }
    }

    @ValidatesTarget(ReturnTargetCardFromGraveyardOrExileToHandEffect.class)
    public void validateReturnTargetCardFromGraveyardOrExile(
            TargetValidationContext ctx, ReturnTargetCardFromGraveyardOrExileToHandEffect effect) {
        if (ctx.targetZone() == Zone.GRAVEYARD) {
            Card targetCard = gameQueryService.findCardInGraveyardById(ctx.gameData(), ctx.targetId());
            if (targetCard == null) {
                throw new IllegalStateException("Target card not found in your graveyard");
            }
            if (ctx.sourceControllerId() != null
                    && !ctx.sourceControllerId().equals(gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId()))) {
                throw new IllegalStateException("Target card must be in your graveyard");
            }
            if (!predicateEvaluationService.matchesCardPredicate(targetCard, effect.graveyardFilter(), null)) {
                throw new IllegalStateException("Target card must be a "
                        + CardPredicateUtils.describeFilter(effect.graveyardFilter()));
            }
            return;
        }
        if (ctx.targetZone() != Zone.EXILE) {
            throw new IllegalStateException("Effect requires a graveyard or exile target");
        }
        Card exiledCard = gameQueryService.findCardInExileById(ctx.gameData(), ctx.targetId());
        if (exiledCard == null) {
            throw new IllegalStateException("Target card not found in exile");
        }
        if (effect.exileOwnedOnly() && ctx.sourceControllerId() != null
                && !ctx.sourceControllerId().equals(gameQueryService.findExileOwnerById(ctx.gameData(), ctx.targetId()))) {
            throw new IllegalStateException("Target must be an exiled card you own");
        }
        if (!predicateEvaluationService.matchesCardPredicate(exiledCard, effect.exileFilter(), null)) {
            throw new IllegalStateException("Target card must be a "
                    + CardPredicateUtils.describeFilter(effect.exileFilter()));
        }
    }
}
