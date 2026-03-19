package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AttachTargetToSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentWhileSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermanentControlTargetValidators {

    private final TargetValidationService tvs;
    private final GameQueryService gameQueryService;

    @ValidatesTarget(GainControlOfEnchantedTargetEffect.class)
    public void validateGainControlOfEnchantedTarget(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    @ValidatesTarget(GainControlOfTargetPermanentUntilEndOfTurnEffect.class)
    public void validateGainControlOfTargetPermanentUntilEndOfTurn(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(GainControlOfTargetPermanentEffect.class)
    public void validateGainControlOfTargetPermanent(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(AttachTargetToSourcePermanentEffect.class)
    public void validateAttachTargetToSourcePermanent(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(GainControlOfTargetAuraEffect.class)
    public void validateGainControlOfTargetAura(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        Permanent target = gameQueryService.findPermanentById(ctx.gameData(), ctx.targetId());
        if (target == null || !target.getCard().hasType(CardType.ENCHANTMENT)
                || !target.getCard().getSubtypes().contains(CardSubtype.AURA)
                || !target.isAttached()) {
            throw new IllegalStateException("Target must be an Aura attached to a permanent");
        }
    }

    @ValidatesTarget(PutTargetOnBottomOfLibraryEffect.class)
    public void validatePutTargetOnBottomOfLibrary(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(PutTargetOnTopOfLibraryEffect.class)
    public void validatePutTargetOnTopOfLibrary(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(GainControlOfTargetPermanentWhileSourceEffect.class)
    public void validateGainControlOfTargetPermanentWhileSource(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(GrantSubtypeToTargetCreatureEffect.class)
    public void validateGrantSubtypeToTargetCreature(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(TargetPlayerGainsControlOfSourceCreatureEffect.class)
    public void validateTargetPlayerGainsControlOfSourceCreature(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }
}
