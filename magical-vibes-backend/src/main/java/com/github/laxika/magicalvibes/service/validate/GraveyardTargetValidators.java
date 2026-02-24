package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GraveyardReturnResolutionService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GraveyardTargetValidators {

    private final TargetValidationService tvs;
    private final GameQueryService gameQueryService;

    @ValidatesTarget(ReturnCardFromGraveyardEffect.class)
    public void validateReturnCardFromGraveyard(TargetValidationContext ctx, ReturnCardFromGraveyardEffect effect) {
        if (!effect.targetGraveyard()) {
            return; // Non-targeting effects choose at resolution time
        }
        if (ctx.targetZone() != Zone.GRAVEYARD) {
            throw new IllegalStateException("Effect requires a graveyard target");
        }
        if (ctx.targetPermanentId() == null) {
            throw new IllegalStateException("Effect requires a target card");
        }
        Card graveyardCard = gameQueryService.findCardInGraveyardById(ctx.gameData(), ctx.targetPermanentId());
        if (graveyardCard == null) {
            throw new IllegalStateException("Target card not found in any graveyard");
        }
        if (effect.filter() != null && !gameQueryService.matchesCardPredicate(graveyardCard, effect.filter(), null)) {
            String label = GraveyardReturnResolutionService.describeFilter(effect.filter());
            throw new IllegalStateException("Target card must be a " + label);
        }
    }

    @ValidatesTarget(PutCardFromOpponentGraveyardOntoBattlefieldEffect.class)
    public void validatePutCardFromOpponentGraveyard(TargetValidationContext ctx) {
        if (ctx.targetZone() != Zone.GRAVEYARD) {
            throw new IllegalStateException("Ability requires a graveyard target");
        }
        if (ctx.targetPermanentId() == null) {
            throw new IllegalStateException("Ability requires a target card");
        }
        Card graveyardCard = gameQueryService.findCardInGraveyardById(ctx.gameData(), ctx.targetPermanentId());
        if (graveyardCard == null) {
            throw new IllegalStateException("Target card not found in any graveyard");
        }
        if (graveyardCard.getType() != CardType.ARTIFACT && graveyardCard.getType() != CardType.CREATURE) {
            throw new IllegalStateException("Target must be an artifact or creature card");
        }
        if (graveyardCard.getManaValue() != ctx.xValue()) {
            throw new IllegalStateException("Target card's mana value must equal X (" + ctx.xValue() + ")");
        }
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetPermanentId());
        UUID controllerId = tvs.findSourcePermanentController(ctx);
        if (graveyardOwnerId != null && graveyardOwnerId.equals(controllerId)) {
            throw new IllegalStateException("Target must be in an opponent's graveyard");
        }
    }
}
