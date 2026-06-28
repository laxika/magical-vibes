package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.effect.DrainLifePerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifePerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LifeTargetValidators {

    private final TargetValidationService tvs;

    @ValidatesTarget(TargetPlayerLosesLifeAndControllerGainsLifeEffect.class)
    public void validateTargetPlayerLosesLifeAndControllerGainsLife(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }

    @ValidatesTarget(TargetPlayerLosesLifeEffect.class)
    public void validateTargetPlayerLosesLife(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }

    @ValidatesTarget(TargetPlayerGainsLifeEffect.class)
    public void validateTargetPlayerGainsLife(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }

    @ValidatesTarget(GiveTargetPlayerPoisonCountersEffect.class)
    public void validateGiveTargetPlayerPoisonCounters(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }

    @ValidatesTarget(DrainLifePerControlledPermanentEffect.class)
    public void validateDrainLifePerControlledPermanent(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }

    @ValidatesTarget(TargetPlayerLosesLifePerControlledPermanentEffect.class)
    public void validateTargetPlayerLosesLifePerControlledPermanent(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }
}
