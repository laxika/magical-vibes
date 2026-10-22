package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetExiledCardIntoHandEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** Validates that a conjure-a-duplicate target is a matching face-up card exiled with the source permanent. */
@Service
public class ConjureDuplicateOfTargetExiledCardIntoHandEffectTargetValidator {

    private final PredicateEvaluationService predicateEvaluationService;

    public ConjureDuplicateOfTargetExiledCardIntoHandEffectTargetValidator(
            PredicateEvaluationService predicateEvaluationService) {
        this.predicateEvaluationService = predicateEvaluationService;
    }

    @ValidatesTarget(ConjureDuplicateOfTargetExiledCardIntoHandEffect.class)
    public void validate(TargetValidationContext ctx, ConjureDuplicateOfTargetExiledCardIntoHandEffect effect) {
        if (ctx.targetZone() != Zone.EXILE || ctx.targetId() == null) {
            throw new IllegalStateException("Effect requires a target card in exile");
        }

        ExiledCardEntry exiled = ctx.gameData().findExiledCard(ctx.targetId());
        if (exiled == null || exiled.faceDown()) {
            throw new IllegalStateException("Target card is not face up in exile");
        }
        if (!predicateEvaluationService.matchesCardPredicate(
                exiled.card(), effect.filter(), ctx.sourceCard() == null ? null : ctx.sourceCard().getId())) {
            throw new IllegalStateException("Target card does not match the effect");
        }

        UUID sourcePermanentId = sourcePermanentId(ctx);
        if (sourcePermanentId == null || !sourcePermanentId.equals(exiled.sourcePermanentId())) {
            throw new IllegalStateException("Target card was not exiled with this permanent");
        }
    }

    private UUID sourcePermanentId(TargetValidationContext ctx) {
        if (ctx.sourcePermanentId() != null) {
            return ctx.sourcePermanentId();
        }
        if (ctx.sourcePermanentSnapshot() != null) {
            return ctx.sourcePermanentSnapshot().getId();
        }
        if (ctx.sourceCard() == null) {
            return null;
        }
        UUID sourceCardId = ctx.sourceCard().getId();
        for (UUID playerId : ctx.gameData().orderedPlayerIds) {
            for (Permanent permanent : ctx.gameData().playerBattlefields.getOrDefault(playerId, List.of())) {
                if (permanent.getCard().getId().equals(sourceCardId)
                        || permanent.getOriginalCard().getId().equals(sourceCardId)) {
                    return permanent.getId();
                }
            }
        }
        return null;
    }
}
