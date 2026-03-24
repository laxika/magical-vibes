package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandWithManaValueConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BounceTargetValidators {

    private final TargetValidationService tvs;

    @ValidatesTarget(ReturnTargetPermanentToHandEffect.class)
    public void validateReturnTargetPermanentToHand(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(ReturnTargetPermanentToHandWithManaValueConditionalEffect.class)
    public void validateReturnTargetPermanentToHandWithConditionalScry(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }
}
