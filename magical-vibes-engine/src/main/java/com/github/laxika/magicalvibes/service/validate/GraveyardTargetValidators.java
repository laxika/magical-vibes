package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureCardInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardCreateTokenEqualToPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardCreateTokensEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardAndSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEmbalmToTargetCreatureCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayTargetCardFromGraveyardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnTargetAndSacrificedCardsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithImprintedCardPredicate;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GraveyardTargetValidators {

    private final TargetValidationService tvs;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;

    @ValidatesTarget(RegisterDelayedReturnTargetAndSacrificedCardsEffect.class)
    public void validateDelayedReturnTargetAndSacrificedCards(TargetValidationContext ctx) {
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
        UUID controllerId = tvs.findSourcePermanentController(ctx);
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
        if (controllerId != null && graveyardOwnerId != null && !graveyardOwnerId.equals(controllerId)) {
            throw new IllegalStateException("Target must be in your graveyard");
        }
    }

    @ValidatesTarget(ReturnCardFromGraveyardEffect.class)
    public void validateReturnCardFromGraveyard(TargetValidationContext ctx, ReturnCardFromGraveyardEffect effect) {
        if (!effect.targetGraveyard()) {
            return; // Non-targeting effects choose at resolution time
        }
        if (ctx.targetZone() != Zone.GRAVEYARD && effect.targetGroup() >= 0) {
            return;
        }
        if (ctx.targetZone() != Zone.GRAVEYARD) {
            throw new IllegalStateException("Effect requires a graveyard target");
        }
        if (ctx.targetId() == null) {
            if (effect.upTo()) {
                return;
            }
            throw new IllegalStateException("Effect requires a target card");
        }
        Card graveyardCard = gameQueryService.findCardInGraveyardById(ctx.gameData(), ctx.targetId());
        if (graveyardCard == null) {
            throw new IllegalStateException("Target card not found in any graveyard");
        }
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
        UUID sourceCardId = ctx.sourceCard() == null ? null : ctx.sourceCard().getId();
        if (effect.filter() != null
                && !(effect.filter() instanceof CardSharesCardTypeWithImprintedCardPredicate)
                && !predicateEvaluationService.matchesCardPredicate(
                graveyardCard, effect.filter(), sourceCardId, ctx.gameData(), graveyardOwnerId)) {
            String label = CardPredicateUtils.describeFilter(effect.filter());
            throw new IllegalStateException("Target card must be a " + label);
        }
        UUID controllerId = ctx.sourceControllerId() != null
                ? ctx.sourceControllerId() : tvs.findSourcePermanentController(ctx);
        if (effect.source() == GraveyardSearchScope.CONTROLLERS_GRAVEYARD) {
            if (controllerId != null && graveyardOwnerId != null && !graveyardOwnerId.equals(controllerId)) {
                throw new IllegalStateException("Target must be in your graveyard");
            }
        } else if (effect.source() == GraveyardSearchScope.OPPONENT_GRAVEYARD
                && controllerId != null && controllerId.equals(graveyardOwnerId)) {
            throw new IllegalStateException("Target must be in an opponent's graveyard");
        }
        if (effect.targetPutIntoGraveyardFromBattlefieldThisTurn()) {
            boolean tracked = graveyardOwnerId != null
                    && ctx.gameData().cardsPutIntoGraveyardFromBattlefieldThisTurn
                            .getOrDefault(graveyardOwnerId, Set.of())
                            .contains(ctx.targetId());
            if (!tracked) {
                throw new IllegalStateException(
                        "Target must be a card put into a graveyard from the battlefield this turn");
            }
        }
        if (effect.targetNotPutIntoGraveyardThisCombat()) {
            boolean tracked = graveyardOwnerId != null
                    && ctx.gameData().cardsPutIntoGraveyardThisCombat
                            .getOrDefault(graveyardOwnerId, Set.of())
                            .contains(ctx.targetId());
            if (tracked) {
                throw new IllegalStateException("Target can't have been put into a graveyard during this combat");
            }
        }
        int requiredManaValue = ctx.xValue()
                + (effect.requiresManaValueEqualsX() ? effect.manaValueXOffset() : 0);
        if (effect.requiresManaValueEqualsX()
                && !ctx.deferCostDerivedXValueChecks()
                && graveyardCard.getManaValue() != requiredManaValue) {
            if (effect.manaValueXOffset() == 0) {
                throw new IllegalStateException("Target card's mana value must equal X (" + ctx.xValue() + ")");
            }
            throw new IllegalStateException("Target card's mana value must equal X plus "
                    + effect.manaValueXOffset() + " (" + requiredManaValue + ")");
        }
        if (effect.requiresManaValueAtMostX()
                && !ctx.deferCostDerivedXValueChecks()
                && graveyardCard.getManaValue() > requiredManaValue) {
            if (effect.manaValueXOffset() == 0) {
                throw new IllegalStateException("Target card's mana value must be " + ctx.xValue() + " or less");
            }
            throw new IllegalStateException("Target card's mana value must be X plus "
                    + effect.manaValueXOffset() + " or less (" + requiredManaValue + ")");
        }
        if (effect.maxManaValueEqualsLifeGainedThisTurn()) {
            UUID sourceControllerId = tvs.findSourcePermanentController(ctx);
            int lifeGained = sourceControllerId == null
                    ? 0 : ctx.gameData().getLifeGainedThisTurn(sourceControllerId);
            if (graveyardCard.getManaValue() > lifeGained) {
                throw new IllegalStateException(
                        "Target card's mana value must be " + lifeGained + " or less");
            }
        }
    }

    @ValidatesTarget(BecomeCopyOfTargetCreatureCardInGraveyardEffect.class)
    public void validateBecomeCopyOfTargetCreatureCardInGraveyard(TargetValidationContext ctx) {
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
        if (!graveyardCard.hasType(CardType.CREATURE)) {
            throw new IllegalStateException("Target must be a creature card");
        }
        UUID controllerId = ctx.sourceControllerId() != null
                ? ctx.sourceControllerId() : tvs.findSourcePermanentController(ctx);
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
        if (controllerId != null && graveyardOwnerId != null && !graveyardOwnerId.equals(controllerId)) {
            throw new IllegalStateException("Target must be in your graveyard");
        }
        if (graveyardCard.getManaValue() != ctx.xValue()) {
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

    @ValidatesTarget(CastTargetInstantOrSorceryFromGraveyardEffect.class)
    public void validateCastTargetInstantOrSorceryFromGraveyard(
            TargetValidationContext ctx, CastTargetInstantOrSorceryFromGraveyardEffect effect) {
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
        if (!graveyardCard.hasType(CardType.INSTANT) && !graveyardCard.hasType(CardType.SORCERY)) {
            throw new IllegalStateException("Target must be an instant or sorcery card");
        }
        if (effect.filter() != null
                && !predicateEvaluationService.matchesCardPredicate(
                        graveyardCard,
                        effect.filter(),
                        ctx.sourceCard() == null ? null : ctx.sourceCard().getId(),
                        ctx.gameData(),
                        gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId()),
                        ctx.sourcePermanentId(), ctx.sourcePowerAtTrigger(), ctx.xValue())) {
            throw new IllegalStateException("Target card does not match the required filter");
        }
        // Opponent-graveyard scope check is enforced in SpellCastingService (which has playerId context)
    }

    @ValidatesTarget(GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect.class)
    public void validateGrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilities(TargetValidationContext ctx) {
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
        if (!graveyardCard.hasType(CardType.CREATURE)) {
            throw new IllegalStateException("Target must be a creature card");
        }
    }

    @ValidatesTarget(GrantFlashbackToTargetGraveyardCardEffect.class)
    public void validateGrantFlashbackToTargetGraveyardCard(
            TargetValidationContext ctx, GrantFlashbackToTargetGraveyardCardEffect effect) {
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
        if (effect.cardTypes().stream().noneMatch(graveyardCard::hasType)) {
            String typeLabel = effect.cardTypes().stream()
                    .map(t -> t.name().toLowerCase())
                    .collect(Collectors.joining(" or "));
            throw new IllegalStateException("Target must be a " + typeLabel + " card");
        }
        UUID controllerId = tvs.findSourcePermanentController(ctx);
        if (controllerId != null) {
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
            if (graveyardOwnerId != null && !graveyardOwnerId.equals(controllerId)) {
                throw new IllegalStateException("Target must be in your graveyard");
            }
        }
    }

    @ValidatesTarget(GrantEmbalmToTargetCreatureCardEffect.class)
    public void validateGrantEmbalmToTargetCreatureCard(TargetValidationContext ctx,
                                                        GrantEmbalmToTargetCreatureCardEffect effect) {
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
        if (!graveyardCard.hasType(CardType.CREATURE)) {
            throw new IllegalStateException("Target must be a creature card");
        }
        UUID controllerId = tvs.findSourcePermanentController(ctx);
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
        if (controllerId != null && graveyardOwnerId != null && !graveyardOwnerId.equals(controllerId)) {
            throw new IllegalStateException("Target must be in your graveyard");
        }
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
        if (effect.filter() != null && !predicateEvaluationService.matchesCardPredicate(graveyardCard, effect.filter(), null)) {
            String label = CardPredicateUtils.describeFilter(effect.filter());
            throw new IllegalStateException("Target must be a " + label);
        }
        if (effect.scope() == GraveyardSearchScope.CONTROLLERS_GRAVEYARD) {
            UUID controllerId = tvs.findSourcePermanentController(ctx);
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
            if (controllerId != null && graveyardOwnerId != null && !graveyardOwnerId.equals(controllerId)) {
                throw new IllegalStateException("Target must be in your graveyard");
            }
        }
    }

    @ValidatesTarget(ExileTargetCardFromGraveyardAndCreateTokenCopyEffect.class)
    public void validateExileTargetCardFromGraveyardAndCreateTokenCopy(
            TargetValidationContext ctx, ExileTargetCardFromGraveyardAndCreateTokenCopyEffect effect) {
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
        UUID sourceCardId = ctx.sourceCard() == null ? null : ctx.sourceCard().getId();
        if (effect.filter() != null && !predicateEvaluationService.matchesCardPredicate(
                graveyardCard, effect.filter(), sourceCardId, ctx.gameData(),
                gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId()))) {
            String label = CardPredicateUtils.describeFilter(effect.filter());
            throw new IllegalStateException("Target must be a " + label);
        }
        if (effect.ownGraveyardOnly()) {
            UUID controllerId = tvs.findSourcePermanentController(ctx);
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
            if (graveyardOwnerId != null && controllerId != null && !graveyardOwnerId.equals(controllerId)) {
                throw new IllegalStateException("Target must be in your graveyard");
            }
        }
        if (effect.targetPutIntoGraveyardFromAnywhereThisTurn()) {
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
            boolean tracked = graveyardOwnerId != null
                    && ctx.gameData().cardsPutIntoGraveyardFromAnywhereThisTurn
                            .getOrDefault(graveyardOwnerId, Set.of())
                            .contains(ctx.targetId());
            if (!tracked) {
                throw new IllegalStateException(
                        "Target must be a creature card put into a graveyard this turn");
            }
        }
    }

    @ValidatesTarget(ExileGraveyardCardsEffect.class)
    public void validateExileGraveyardCards(TargetValidationContext ctx, ExileGraveyardCardsEffect effect) {
        // Runs unconditionally for the class; gate the per-scope checks. The opponent-multi-card scope
        // is validated separately in TargetLegalityService.validateMultiTargetGraveyardAbility, and the
        // OWN / ALL_* scopes take no single validated target here.
        if (effect.scope() == GraveyardExileScope.TARGET_PLAYER_ENTIRE) {
            tvs.requireTargetPlayer(ctx);
            return;
        }
        if (effect.scope() != GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD) {
            return;
        }
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
        if (effect.filter() != null && !predicateEvaluationService.matchesCardPredicate(graveyardCard, effect.filter(), null)) {
            throw new IllegalStateException("Target must be a " + CardPredicateUtils.describeFilter(effect.filter()));
        }
    }

    @ValidatesTarget(ExileTargetCreatureCardCreateTokensEqualToToughnessEffect.class)
    public void validateExileTargetCreatureCardCreateTokens(TargetValidationContext ctx) {
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
    }

    @ValidatesTarget(ExileTargetCreatureCardCreateTokenEqualToPowerToughnessEffect.class)
    public void validateExileTargetCreatureCardCreateToken(TargetValidationContext ctx) {
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
        UUID controllerId = tvs.findSourcePermanentController(ctx);
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
        if (controllerId != null && graveyardOwnerId != null && !graveyardOwnerId.equals(controllerId)) {
            throw new IllegalStateException("Target must be in your graveyard");
        }
    }

    @ValidatesTarget(ExileTargetCreatureCardFromGraveyardGainLifeEqualToToughnessEffect.class)
    public void validateExileTargetCreatureCardGainLife(TargetValidationContext ctx) {
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
        if (!graveyardCard.hasType(CardType.CREATURE)) {
            throw new IllegalStateException("Target must be a creature card");
        }
    }

    @ValidatesTarget(ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect.class)
    public void validateExileTargetCardFromGraveyardMayPlay(
            TargetValidationContext ctx, ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect effect) {
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
        if (effect.filter() != null && !predicateEvaluationService.matchesCardPredicate(graveyardCard, effect.filter(), null)) {
            String label = CardPredicateUtils.describeFilter(effect.filter());
            throw new IllegalStateException("Target must be a " + label);
        }
        if (effect.ownGraveyardOnly()) {
            UUID controllerId = tvs.findSourcePermanentController(ctx);
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
            if (graveyardOwnerId != null && controllerId != null && !graveyardOwnerId.equals(controllerId)) {
                throw new IllegalStateException("Target must be in your graveyard");
            }
        }
    }

    @ValidatesTarget(PlayTargetCardFromGraveyardWithoutPayingManaCostEffect.class)
    public void validatePlayTargetCardFromGraveyardWithoutPaying(
            TargetValidationContext ctx, PlayTargetCardFromGraveyardWithoutPayingManaCostEffect effect) {
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
        if (effect.filter() != null && !predicateEvaluationService.matchesCardPredicate(graveyardCard, effect.filter(), null)) {
            String label = CardPredicateUtils.describeFilter(effect.filter());
            throw new IllegalStateException("Target must be a " + label);
        }
        UUID controllerId = tvs.findSourcePermanentController(ctx);
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
        if (graveyardOwnerId != null && controllerId != null && !graveyardOwnerId.equals(controllerId)) {
            throw new IllegalStateException("Target must be in your graveyard");
        }
    }

    @ValidatesTarget(ExileTargetInstantOrSorceryFromOpponentGraveyardMayCastEffect.class)
    public void validateExileTargetInstantOrSorceryFromOpponentGraveyardMayCast(TargetValidationContext ctx) {
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
        if (!graveyardCard.hasType(CardType.INSTANT) && !graveyardCard.hasType(CardType.SORCERY)) {
            throw new IllegalStateException("Target must be an instant or sorcery card");
        }
        UUID controllerId = tvs.findSourcePermanentController(ctx);
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
        if (graveyardOwnerId != null && controllerId != null && graveyardOwnerId.equals(controllerId)) {
            throw new IllegalStateException("Target must be in an opponent's graveyard");
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
    public void validatePutCardFromOpponentGraveyard(TargetValidationContext ctx,
                                                     PutCardFromOpponentGraveyardOntoBattlefieldEffect effect) {
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
        if (effect.filter() != null
                && !predicateEvaluationService.matchesCardPredicate(graveyardCard, effect.filter(), null)) {
            String label = CardPredicateUtils.describeFilter(effect.filter());
            throw new IllegalStateException("Target must be a " + label);
        }
        if (effect.requireManaValueEqualsX() && graveyardCard.getManaValue() != ctx.xValue()) {
            throw new IllegalStateException("Target card's mana value must equal X (" + ctx.xValue() + ")");
        }
        if (effect.maxManaValue() != null) {
            UUID controllerId = ctx.sourceControllerId() != null
                    ? ctx.sourceControllerId() : tvs.findSourcePermanentController(ctx);
            int maxManaValue = amountEvaluationService.evaluate(
                    ctx.gameData(), effect.maxManaValue(), AmountContext.forCasting(controllerId));
            if (graveyardCard.getManaValue() > maxManaValue) {
                throw new IllegalStateException(
                        "Target card's mana value must be " + maxManaValue + " or less");
            }
        }
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(ctx.gameData(), ctx.targetId());
        UUID controllerId = tvs.findSourcePermanentController(ctx);
        if (graveyardOwnerId != null && graveyardOwnerId.equals(controllerId)) {
            throw new IllegalStateException("Target must be in an opponent's graveyard");
        }
    }
}
