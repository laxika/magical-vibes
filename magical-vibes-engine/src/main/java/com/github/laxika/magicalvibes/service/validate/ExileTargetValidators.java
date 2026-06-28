package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromExileToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExileTargetValidators {

    private final GameQueryService gameQueryService;

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
        if (effect.filter() != null && !gameQueryService.matchesCardPredicate(exiledCard, effect.filter(), null)) {
            String label = CardPredicateUtils.describeFilter(effect.filter());
            throw new IllegalStateException("Target card must be a " + label);
        }
    }
}
