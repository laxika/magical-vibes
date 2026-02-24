package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LibraryTargetValidators {

    private final TargetValidationService tvs;

    @ValidatesTarget(MillTargetPlayerEffect.class)
    public void validateMillTargetPlayer(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }

    @ValidatesTarget(RevealTopCardOfLibraryEffect.class)
    public void validateRevealTopCardOfLibrary(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }
}
