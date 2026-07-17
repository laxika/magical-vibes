package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.effect.DrainLifePerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LifeTargetValidators {

    private final TargetValidationService tvs;

    @ValidatesTarget(LoseLifeEffect.class)
    public void validateLoseLife(TargetValidationContext ctx, LoseLifeEffect effect) {
        // Only the target-player recipient targets a player; controller / each-player / each-opponent
        // take no target and must not have a player-target requirement forced on them.
        if (effect.recipient() == LoseLifeRecipient.TARGET_PLAYER) {
            tvs.requireTargetPlayer(ctx);
        }
    }

    @ValidatesTarget(TargetPlayerGainsLifeEffect.class)
    public void validateTargetPlayerGainsLife(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }

    @ValidatesTarget(GivePoisonCountersEffect.class)
    public void validateGivePoisonCounters(TargetValidationContext ctx, GivePoisonCountersEffect effect) {
        // Only the target-player recipient targets a player; controller / each-player / enchanted-
        // permanent-controller take no player target and must not have one forced on them.
        if (effect.recipient() == PoisonRecipient.TARGET_PLAYER) {
            tvs.requireTargetPlayer(ctx);
        }
    }

    @ValidatesTarget(DrainLifePerControlledPermanentEffect.class)
    public void validateDrainLifePerControlledPermanent(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }
}
