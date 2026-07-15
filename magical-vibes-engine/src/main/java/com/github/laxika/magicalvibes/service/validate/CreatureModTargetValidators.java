package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Escape-hatch target validators for creature-modifying effects whose target is NOT chosen through
 * the single-target pipeline.
 *
 * <p>Every effect that declares its targeting through a {@code CardEffect.targetSpec()} (boosts,
 * keyword grants, combat-shaping, animate/tap/untap, fights, land-type granting, …) was migrated to
 * the declarative {@code TargetSpec} interpreter in {@code TargetValidationService} and no longer
 * needs a hand-written validator here.
 *
 * <p>What remains are the two boost effects that target through the equip/attach mechanism
 * ({@link StaticBoostEffect}, {@link AttachedBoostEffect}): they override no legacy targeting method
 * and expose no {@code targetSpec()} category, so their "the attached/boosted permanent must be a
 * creature" restriction is enforced here instead.
 */
@Service
@RequiredArgsConstructor
public class CreatureModTargetValidators {

    private final TargetValidationService tvs;

    @ValidatesTarget(StaticBoostEffect.class)
    public void validateStaticBoost(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    @ValidatesTarget(AttachedBoostEffect.class)
    public void validateAttachedBoost(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }
}
