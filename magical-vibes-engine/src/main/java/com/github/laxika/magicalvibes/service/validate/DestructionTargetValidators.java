package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndBoostSelfByManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndGainLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetThenRevealUntilTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetThenRevealUntilTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DestructionTargetValidators {

    private final TargetValidationService tvs;
    private final GameQueryService gameQueryService;

    // The validator is keyed by effect class and runs unconditionally, so only the
    // target-player recipient requires a targeted player; controller / each-player / each-opponent
    // recipients take no target.
    @ValidatesTarget(SacrificePermanentsEffect.class)
    public void validateSacrificePermanents(TargetValidationContext ctx, SacrificePermanentsEffect effect) {
        if (effect.recipient() == SacrificeRecipient.TARGET_PLAYER) {
            tvs.requireTargetPlayer(ctx);
        }
    }

    @ValidatesTarget(DestroyCreatureBlockingThisEffect.class)
    public void validateDestroyCreatureBlockingThis(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        Permanent target = gameQueryService.findPermanentById(ctx.gameData(), ctx.targetId());
        if (target == null || !gameQueryService.isCreature(ctx.gameData(), target) || !target.isBlocking()) {
            throw new IllegalStateException("Target must be a creature blocking this creature");
        }
        int sourceIndex = tvs.findSourcePermanentIndex(ctx);
        if (sourceIndex < 0 || !target.getBlockingTargets().contains(sourceIndex)) {
            throw new IllegalStateException("Target must be a creature blocking this creature");
        }
    }

    @ValidatesTarget(DestroyTargetPermanentEffect.class)
    public void validateDestroyTargetPermanent(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect.class)
    public void validateDestroyTargetAndControllerLosesLifePerCreatureDeaths(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DestroyTargetPermanentAndBoostSelfByManaValueEffect.class)
    public void validateDestroyTargetArtifactAndBoostSelfByManaValue(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DestroyTargetPermanentAndGainLifeEqualToManaValueEffect.class)
    public void validateDestroyTargetPermanentAndGainLifeEqualToManaValue(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(SacrificeTargetThenRevealUntilTypeToBattlefieldEffect.class)
    public void validateSacrificeTargetThenRevealUntilType(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DestroyTargetThenRevealUntilTypeToBattlefieldEffect.class)
    public void validateDestroyTargetThenRevealUntilType(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }
}
