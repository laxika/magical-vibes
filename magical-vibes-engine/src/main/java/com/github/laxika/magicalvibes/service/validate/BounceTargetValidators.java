package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.effect.BounceScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandWithManaValueConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandAtEndStepEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BounceTargetValidators {

    private final TargetValidationService tvs;

    @ValidatesTarget(ReturnToHandEffect.class)
    public void validateReturnToHand(TargetValidationContext ctx, ReturnToHandEffect effect) {
        // Validator is class-keyed and runs unconditionally; only the TARGET scope actually
        // targets a battlefield permanent. The other scopes (SELF / ALL_MATCHING /
        // TARGET_PLAYERS_*) take no permanent target and must not require one.
        if (effect.scope() == BounceScope.TARGET) {
            tvs.requireBattlefieldTarget(ctx);
        }
    }

    @ValidatesTarget(ReturnTargetPermanentToHandWithManaValueConditionalEffect.class)
    public void validateReturnTargetPermanentToHandWithConditionalScry(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect.class)
    public void validateReturnTargetPermanentToHandOrLibraryTopByPredicate(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(ReturnTargetPermanentToHandAtEndStepEffect.class)
    public void validateReturnTargetPermanentToHandAtEndStep(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }
}
