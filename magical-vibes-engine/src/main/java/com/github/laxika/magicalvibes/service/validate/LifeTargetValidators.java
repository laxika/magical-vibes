package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
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
        // Only a declared target player targets a player; the other subjects take no target and must not
        // have a player-target requirement forced on them.
        if (effect.player().declaresTarget()) {
            tvs.requireTargetPlayer(ctx);
        }
    }

    @ValidatesTarget(GainLifeEffect.class)
    public void validateGainLife(TargetValidationContext ctx, GainLifeEffect effect) {
        // A declared target player must be a player; a gain for you whose amount reads a target player
        // (targetsPlayer) keeps the card's own target filter, as before.
        if (effect.player().declaresTarget()) {
            tvs.requireTargetPlayer(ctx);
        }
    }

    @ValidatesTarget(GivePoisonCountersEffect.class)
    public void validateGivePoisonCounters(TargetValidationContext ctx, GivePoisonCountersEffect effect) {
        if (effect.player().declaresTarget()) {
            tvs.requireTargetPlayer(ctx);
        }
    }
}
