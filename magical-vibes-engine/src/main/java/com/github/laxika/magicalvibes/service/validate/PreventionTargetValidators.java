package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Escape-hatch target validator for divided damage-prevention. The purely structural prevention
 * validators ("any target" prevention, Harm's Way redirection, "prevent all damage to/by target
 * creature") were migrated to declarative {@code targetSpec()} values and their validators deleted.
 * Only {@link PreventDividedDamageEffect} remains here: its targets are announced onto
 * {@code StackEntry.damageAssignments} (like {@code DealDividedDamageEffect} CHOSEN mode), so
 * {@code targetId} is null on that path — a null tolerance the declarative spec interpreter (which
 * requires a target) cannot express.
 */
@Service
@RequiredArgsConstructor
public class PreventionTargetValidators {

    private final TargetValidationService tvs;
    private final GameQueryService gameQueryService;

    // Remedy: divided prevention among target creatures and/or players. Targets are announced onto
    // StackEntry.damageAssignments (like DealDividedDamageEffect CHOSEN mode), so targetId is null on
    // that path — tolerate it, mirroring DealDividedDamage's validator.
    @ValidatesTarget(PreventDividedDamageEffect.class)
    public void validatePreventDividedDamage(TargetValidationContext ctx) {
        if (ctx.targetId() == null) {
            return;
        }
        if (ctx.gameData().playerIds.contains(ctx.targetId())) {
            return;
        }
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }
}
