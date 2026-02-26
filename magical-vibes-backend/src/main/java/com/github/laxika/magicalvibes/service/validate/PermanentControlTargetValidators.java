package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEquipmentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
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

    @ValidatesTarget(GainControlOfTargetCreatureUntilEndOfTurnEffect.class)
    public void validateGainControlOfTargetCreatureUntilEndOfTurn(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    @ValidatesTarget(GainControlOfTargetEquipmentUntilEndOfTurnEffect.class)
    public void validateGainControlOfTargetEquipmentUntilEndOfTurn(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(GainControlOfTargetAuraEffect.class)
    public void validateGainControlOfTargetAura(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        Permanent target = gameQueryService.findPermanentById(ctx.gameData(), ctx.targetPermanentId());
        if (target == null || target.getCard().getType() != CardType.ENCHANTMENT
                || !target.getCard().getSubtypes().contains(CardSubtype.AURA)
                || target.getAttachedTo() == null) {
            throw new IllegalStateException("Target must be an Aura attached to a permanent");
        }
    }

    @ValidatesTarget(PutTargetOnBottomOfLibraryEffect.class)
    public void validatePutTargetOnBottomOfLibrary(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        Permanent target = gameQueryService.findPermanentById(ctx.gameData(), ctx.targetPermanentId());
        if (target == null || !gameQueryService.isCreature(ctx.gameData(), target)) {
            throw new IllegalStateException("Target must be a creature");
        }
    }

    @ValidatesTarget(TargetPlayerGainsControlOfSourceCreatureEffect.class)
    public void validateTargetPlayerGainsControlOfSourceCreature(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }
}
