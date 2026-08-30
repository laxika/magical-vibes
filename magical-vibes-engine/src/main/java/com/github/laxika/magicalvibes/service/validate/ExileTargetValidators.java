package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExchangeTargetAnteCardWithTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.AdjustChosenCounterOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.AdjustTimeCountersOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.BounceScope;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardFromExileIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromExileToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardFromExileIntoOwnersLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Escape-hatch validator for the exile family. The structural "exile target permanent / creature"
 * effects now carry a harmful {@code TargetSpec} interpreted by {@code TargetValidationService}
 * (PERMANENT / CREATURE, honouring protection); exile-zone card effects retain validators because
 * the EXILE category needs face-up and effect-specific checks beyond structural target typing.
 */
@Service
@RequiredArgsConstructor
public class ExileTargetValidators {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @ValidatesTarget(AdjustChosenCounterOnTargetEffect.class)
    public void validateAdjustChosenCounterOnTarget(TargetValidationContext ctx,
                                                     AdjustChosenCounterOnTargetEffect effect) {
        if (ctx.targetZone() != Zone.EXILE) {
            return;
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Effect requires a target card");
        }
        ExiledCardEntry exiled = ctx.gameData().findExiledCard(ctx.targetId());
        if (exiled == null || exiled.faceDown()) {
            throw new IllegalStateException("Target card must be face up in exile");
        }
        Integer timeCounters = ctx.gameData().exiledCardTimeCounters.get(ctx.targetId());
        if (timeCounters == null || timeCounters <= 0) {
            throw new IllegalStateException("Target card must be suspended");
        }
        if (effect.suspendedCardOwnedOnly()) {
            UUID controllerId = ctx.sourceControllerId() != null
                    ? ctx.sourceControllerId() : findSourceController(ctx);
            if (controllerId == null || !controllerId.equals(exiled.ownerId())) {
                throw new IllegalStateException("Target card must be owned by the ability controller");
            }
        }
    }

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
        if (effect.ownedOnly() && ctx.sourceControllerId() != null) {
            ExiledCardEntry exiledEntry = ctx.gameData().findExiledCard(ctx.targetId());
            if (exiledEntry == null || !ctx.sourceControllerId().equals(exiledEntry.ownerId())) {
                throw new IllegalStateException("Target must be an exiled card you own");
            }
        }
        ExiledCardEntry exiled = ctx.gameData().findExiledCard(ctx.targetId());
        if (exiled == null || exiled.faceDown()) {
            throw new IllegalStateException("Target card must be face up in exile");
        }
        if (effect.ownedOnly() && ctx.sourceControllerId() != null
                && !ctx.gameData().getPlayerExiledCards(ctx.sourceControllerId()).stream()
                .anyMatch(card -> card.getId().equals(ctx.targetId()))) {
            throw new IllegalStateException("Target card must be in the controller's exile");
        }
        if (effect.filter() != null && !predicateEvaluationService.matchesCardPredicate(exiledCard, effect.filter(), null)) {
            String label = CardPredicateUtils.describeFilter(effect.filter());
            throw new IllegalStateException("Target card must be a " + label);
        }
    }

    @ValidatesTarget(ReturnToHandEffect.class)
    public void validateReturnToHandTarget(TargetValidationContext ctx, ReturnToHandEffect effect) {
        if (effect.scope() != BounceScope.TARGET_NONLAND_PERMANENT_OR_SUSPENDED_CARD
                || ctx.targetZone() != Zone.EXILE) {
            return;
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Effect requires a target card");
        }
        ExiledCardEntry exiled = ctx.gameData().findExiledCard(ctx.targetId());
        Integer timeCounters = ctx.gameData().exiledCardTimeCounters.get(ctx.targetId());
        if (exiled == null || exiled.faceDown() || timeCounters == null || timeCounters <= 0) {
            throw new IllegalStateException("Target card must be suspended");
        }
    }

    @ValidatesTarget(ExchangeTargetAnteCardWithTopOfLibraryEffect.class)
    public void validateExchangeTargetAnteCard(TargetValidationContext ctx,
                                               ExchangeTargetAnteCardWithTopOfLibraryEffect effect) {
        if (ctx.targetZone() != Zone.EXILE) {
            throw new IllegalStateException("Effect requires an ante target");
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Effect requires a target card");
        }
        ExiledCardEntry anteEntry = ctx.gameData().findExiledCard(ctx.targetId());
        if (anteEntry == null || !ctx.gameData().antedCardIds.contains(ctx.targetId())) {
            throw new IllegalStateException("Target card must be in the ante");
        }
        if (ctx.sourceControllerId() != null && !ctx.sourceControllerId().equals(anteEntry.ownerId())) {
            throw new IllegalStateException("Target card must be one you own in the ante");
        }
    }

    @ValidatesTarget(AdjustTimeCountersOnTargetEffect.class)
    public void validateAdjustTimeCountersOnTarget(TargetValidationContext ctx,
                                                   AdjustTimeCountersOnTargetEffect effect) {
        if (ctx.targetZone() != Zone.EXILE) {
            return;
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Effect requires a target card");
        }
        ExiledCardEntry exiled = ctx.gameData().findExiledCard(ctx.targetId());
        Integer timeCounters = ctx.gameData().exiledCardTimeCounters.get(ctx.targetId());
        if (exiled == null || exiled.faceDown() || timeCounters == null || timeCounters <= 0) {
            throw new IllegalStateException("Target card must be suspended");
        }
    }

    @ValidatesTarget(PutTargetCardFromExileIntoOwnersGraveyardEffect.class)
    public void validatePutTargetCardFromExileIntoOwnersGraveyard(
            TargetValidationContext ctx, PutTargetCardFromExileIntoOwnersGraveyardEffect effect) {
        if (ctx.targetZone() != Zone.EXILE) {
            throw new IllegalStateException("Effect requires an exile target");
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Effect requires a target card");
        }
        ExiledCardEntry exiled = ctx.gameData().findExiledCard(ctx.targetId());
        if (exiled == null || exiled.faceDown()) {
            throw new IllegalStateException("Target card must be face up in exile");
        }
    }

    @ValidatesTarget(ShuffleTargetCardFromExileIntoOwnersLibraryEffect.class)
    public void validateShuffleTargetCardFromExileIntoOwnersLibrary(
            TargetValidationContext ctx, ShuffleTargetCardFromExileIntoOwnersLibraryEffect effect) {
        if (ctx.targetZone() != Zone.EXILE) {
            throw new IllegalStateException("Effect requires an exile target");
        }
        if (ctx.targetId() == null) {
            throw new IllegalStateException("Effect requires a target card");
        }
        ExiledCardEntry exiled = ctx.gameData().findExiledCard(ctx.targetId());
        if (exiled == null || exiled.faceDown()) {
            throw new IllegalStateException("Target card must be face up in exile");
        }
    }

    private UUID findSourceController(TargetValidationContext ctx) {
        if (ctx.sourceCard() == null) {
            return null;
        }
        for (UUID playerId : ctx.gameData().orderedPlayerIds) {
            if (ctx.gameData().playerBattlefields.getOrDefault(playerId, java.util.List.of()).stream()
                    .anyMatch(permanent -> permanent.getCard() == ctx.sourceCard())) {
                return playerId;
            }
        }
        return null;
    }
}
