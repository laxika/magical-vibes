package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAndExileFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MillBottomOfTargetLibraryConditionalTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
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

    @ValidatesTarget(MillEffect.class)
    public void validateMill(TargetValidationContext ctx, MillEffect effect) {
        // Only the target-player recipient targets a player; controller / each-opponent take no
        // player target and must not have a player-target requirement forced on them.
        if (effect.recipient() == MillRecipient.TARGET_PLAYER) {
            tvs.requireTargetPlayer(ctx);
        }
    }

    @ValidatesTarget(RevealTopCardOfLibraryEffect.class)
    public void validateRevealTopCardOfLibrary(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }

    @ValidatesTarget(ChooseCardNameAndExileFromZonesEffect.class)
    public void validateChooseCardNameAndExileFromZones(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }

    @ValidatesTarget(ExileTargetPlayerGraveyardEffect.class)
    public void validateExileTargetPlayerGraveyard(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }

    @ValidatesTarget(MillBottomOfTargetLibraryConditionalTokenEffect.class)
    public void validateMillBottomOfTargetLibraryConditionalToken(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }
}
