package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
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
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Effect requires a target card");
        }
        Card graveyardCard = gameQueryService.findCardInGraveyardById(ctx.gameData(), ctx.targetId());
        if (graveyardCard == null) {
            throw new IllegalStateException("Target card not found in any graveyard");
        }
        if (effect.filter() != null && !gameQueryService.matchesCardPredicate(graveyardCard, effect.filter(), null)) {
            String label = CardPredicateUtils.describeFilter(effect.filter());
            throw new IllegalStateException("Target card must be a " + label);
        }
        if (effect.requiresManaValueEqualsX() && graveyardCard.getManaValue() != ctx.xValue()) {
            throw new IllegalStateException("Target card's mana value must equal X (" + ctx.xValue() + ")");
        }
    }

    @ValidatesTarget(PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect.class)
    public void validatePutCreatureFromOpponentGraveyardWithExile(TargetValidationContext ctx) {
        if (ctx.targetZone() != Zone.GRAVEYARD) {
            throw new IllegalStateException("Spell requires a graveyard target");
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Spell requires a target card");
        }
        Card graveyardCard = gameQueryService.findCardInGraveyardById(ctx.gameData(), ctx.targetId());
        if (graveyardCard == null) {
            throw new IllegalStateException("Target card not found in any graveyard");
        }
        if (!graveyardCard.hasType(CardType.CREATURE)) {
            throw new IllegalStateException("Target must be a creature card");
        }
        // Opponent-graveyard check is enforced in SpellCastingService (which has playerId context)
    }

    @ValidatesTarget(ExileTargetCardFromGraveyardAndImprintOnSourceEffect.class)
    public void validateExileTargetCardFromGraveyardAndImprint(TargetValidationContext ctx, ExileTargetCardFromGraveyardAndImprintOnSourceEffect effect) {
        if (ctx.targetZone() != Zone.GRAVEYARD) {
            throw new IllegalStateException("Ability requires a graveyard target");
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Ability requires a target card");
        }
        Card graveyardCard = gameQueryService.findCardInGraveyardById(ctx.gameData(), ctx.targetId());
        if (graveyardCard == null) {
            throw new IllegalStateException("Target card not found in any graveyard");
        }
        if (effect.filter() != null && !gameQueryService.matchesCardPredicate(graveyardCard, effect.filter(), null)) {
            String label = CardPredicateUtils.describeFilter(effect.filter());
            throw new IllegalStateException("Target must be a " + label);
        }
    }

    @ValidatesTarget(ExileTargetCardFromGraveyardEffect.class)
    public void validateExileTargetCardFromGraveyard(TargetValidationContext ctx, ExileTargetCardFromGraveyardEffect effect) {
        if (ctx.targetZone() != Zone.GRAVEYARD) {
            throw new IllegalStateException("Ability requires a graveyard target");
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Ability requires a target card");
        }
        Card graveyardCard = gameQueryService.findCardInGraveyardById(ctx.gameData(), ctx.targetId());
        if (graveyardCard == null) {
            throw new IllegalStateException("Target card not found in any graveyard");
        }
        if (effect.requiredType() != null && !graveyardCard.hasType(effect.requiredType())) {
            throw new IllegalStateException("Target must be a " + effect.requiredType().name().toLowerCase() + " card");
        }
    }

    @ValidatesTarget(ExileTargetGraveyardCardAndSameNameFromZonesEffect.class)
    public void validateExileTargetGraveyardCardAndSameName(TargetValidationContext ctx) {
        if (ctx.targetZone() != Zone.GRAVEYARD) {
            throw new IllegalStateException("Spell requires a graveyard target");
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Spell requires a target card");
        }
        Card graveyardCard = gameQueryService.findCardInGraveyardById(ctx.gameData(), ctx.targetId());
        if (graveyardCard == null) {
            throw new IllegalStateException("Target card not found in any graveyard");
        }
        if (graveyardCard.hasType(CardType.LAND)
                && graveyardCard.getSupertypes().contains(CardSupertype.BASIC)) {
            throw new IllegalStateException("Target must not be a basic land card");
        }
    }

    @ValidatesTarget(PutCardFromOpponentGraveyardOntoBattlefieldEffect.class)
    public void validatePutCardFromOpponentGraveyard(TargetValidationContext ctx) {
        if (ctx.targetZone() != Zone.GRAVEYARD) {
            throw new IllegalStateException("Ability requires a graveyard target");
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Ability requires a target card");
        }
        Card graveyardCard = gameQueryService.findCardInGraveyardById(ctx.gameData(), ctx.targetId());
        if (graveyardCard == null) {
            throw new IllegalStateException("Target card not found in any graveyard");
        }
        if (!graveyardCard.hasType(CardType.ARTIFACT) && !graveyardCard.hasType(CardType.CREATURE)) {
            throw new IllegalStateException("Target must be an artifact or creature card");
        }
        if (graveyardCard.getManaValue() != ctx.xValue()) {
            throw new IllegalStateException("Target card's mana value must equal X (" + ctx.xValue() + ")");
        }
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
        UUID controllerId = tvs.findSourcePermanentController(ctx);
        if (graveyardOwnerId != null && graveyardOwnerId.equals(controllerId)) {
            throw new IllegalStateException("Target must be in an opponent's graveyard");
        }
    }
}
