package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromExileToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExileTargetValidators {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetValidationService tvs;

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

    // ===== "Exile target permanent" family — any permanent, protection honoured (harmful) =====

    @ValidatesTarget(ExileTargetPermanentEffect.class)
    public void validateExileTargetPermanent(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(ExileTargetPermanentAndTrackWithSourceEffect.class)
    public void validateExileTargetPermanentAndTrackWithSource(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(ExileTargetPermanentMayPlayUntilNextTurnEffect.class)
    public void validateExileTargetPermanentMayPlayUntilNextTurn(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    // ===== "Exile target creature" family =====

    @ValidatesTarget(ExileTargetCreatureAndAllWithSameNameEffect.class)
    public void validateExileTargetCreatureAndAllWithSameName(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect.class)
    public void validateExileOwnGraveyardCardThenDamageTargetCreatureController(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(MarkTargetCreatureExileInsteadOfDieThisTurnEffect.class)
    public void validateMarkTargetCreatureExileInsteadOfDie(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
        tvs.checkProtection(ctx, target);
    }
}
