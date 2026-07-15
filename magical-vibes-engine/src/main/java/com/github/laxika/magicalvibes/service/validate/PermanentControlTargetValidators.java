package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Escape-hatch validators for the control-change / library-tuck family. The structural
 * single-target effects now carry a benign {@code TargetSpec} interpreted by
 * {@code TargetValidationService} (PERMANENT / CREATURE); only the three effects below retain a
 * validator: an Aura target needs an attachment-state check, "put target on top of library" reads
 * its own {@code canTargetPermanent()} (derived from its spec until step 10 rewrites the reader),
 * and the player-gains-control effect needs the {@code requireTargetPlayer} guard the no-op PLAYER
 * category cannot reproduce.
 */
@Service
@RequiredArgsConstructor
public class PermanentControlTargetValidators {

    private final TargetValidationService tvs;
    private final GameQueryService gameQueryService;

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

    @ValidatesTarget(PutTargetOnTopOfLibraryEffect.class)
    public void validatePutTargetOnTopOfLibrary(TargetValidationContext ctx, PutTargetOnTopOfLibraryEffect effect) {
        if (effect.canTargetPermanent()) {
            tvs.requireBattlefieldTarget(ctx);
        }
    }

    @ValidatesTarget(TargetPlayerGainsControlOfSourceCreatureEffect.class)
    public void validateTargetPlayerGainsControlOfSourceCreature(TargetValidationContext ctx) {
        tvs.requireTargetPlayer(ctx);
    }
}
