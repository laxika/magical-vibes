package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Escape-hatch validators for the destroy / sacrifice family. The structural single-target
 * destroy / sacrifice effects now carry a harmful {@code TargetSpec} interpreted by
 * {@code TargetValidationService} (PERMANENT / CREATURE / LAND, all honouring protection); only the
 * two effects below retain a validator because they encode logic beyond target-type structure:
 * a conditional player requirement the no-op PLAYER category cannot reproduce, and a
 * combat-relation ("blocking this") state check.
 */
@Service
@RequiredArgsConstructor
public class DestructionTargetValidators {

    private final TargetValidationService tvs;
    private final GameQueryService gameQueryService;

    // The validator is keyed by effect class and runs unconditionally, so only the
    // target-player recipient requires a targeted player; controller / each-player / each-opponent
    // recipients take no target. The PLAYER TargetSpec category is a no-op in the interpreter, so
    // this guard cannot be expressed declaratively.
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
}
