package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DamageTargetValidators {

    private final TargetValidationService tvs;
    private final GameQueryService gameQueryService;

    @ValidatesTarget(DealXDamageToTargetCreatureEffect.class)
    public void validateDealXDamageToTargetCreature(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDamageToTargetCreatureEffect.class)
    public void validateDealDamageToTargetCreature(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect.class)
    public void validateDealDamageToTargetCreatureEqualToSubtypeCount(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDamageToAnyTargetEffect.class)
    public void validateDealDamageToAnyTarget(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        if (ctx.gameData().playerIds.contains(ctx.targetPermanentId())) {
            return;
        }
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        boolean validPermanentType = gameQueryService.isCreature(ctx.gameData(), target)
                || target.getCard().getType() == CardType.PLANESWALKER;
        if (!validPermanentType) {
            throw new IllegalStateException("Target must be a creature, planeswalker, or player");
        }
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDamageToTargetAndTheirCreaturesEffect.class)
    public void validateDealDamageToTargetAndTheirCreatures(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        if (ctx.gameData().playerIds.contains(ctx.targetPermanentId())) {
            return;
        }
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        if (target.getCard().getType() != CardType.PLANESWALKER
                && !target.getCard().getAdditionalTypes().contains(CardType.PLANESWALKER)) {
            throw new IllegalStateException("Target must be a player or planeswalker");
        }
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect.class)
    public void validateDealDamageToAnyTargetEqualToSubtypeCountAndGainLife(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        if (ctx.gameData().playerIds.contains(ctx.targetPermanentId())) {
            return;
        }
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        boolean validPermanentType = gameQueryService.isCreature(ctx.gameData(), target)
                || target.getCard().getType() == CardType.PLANESWALKER;
        if (!validPermanentType) {
            throw new IllegalStateException("Target must be a creature, planeswalker, or player");
        }
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDamageToTargetPlayerEffect.class)
    public void validateDealDamageToTargetPlayer(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }

    @ValidatesTarget(DealDamageToTargetPlayerByHandSizeEffect.class)
    public void validateDealDamageToTargetPlayerByHandSize(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }
}
