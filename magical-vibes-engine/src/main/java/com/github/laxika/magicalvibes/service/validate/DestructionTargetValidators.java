package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DestroyAttachmentsOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetThenRevealUntilTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetThenRevealUntilTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetLandAndDamageControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DestructionTargetValidators {

    private final TargetValidationService tvs;
    private final GameQueryService gameQueryService;

    // The validator is keyed by effect class and runs unconditionally, so only the
    // target-player recipient requires a targeted player; controller / each-player / each-opponent
    // recipients take no target.
    @ValidatesTarget(SacrificePermanentsEffect.class)
    public void validateSacrificePermanents(TargetValidationContext ctx, SacrificePermanentsEffect effect) {
        if (effect.recipient() == SacrificeRecipient.TARGET_PLAYER) {
            tvs.requireTargetPlayer(ctx);
        }
    }

    @ValidatesTarget(DestroyCreatureBlockingThisEffect.class)
    public void validateDestroyCreatureBlockingThis(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        Permanent target = gameQueryService.findPermanentById(ctx.gameData(), ctx.targetId());
        if (target == null || !gameQueryService.isCreature(ctx.gameData(), target) || !target.isBlocking()) {
            throw new IllegalStateException("Target must be a creature blocking this creature");
        }
        int sourceIndex = tvs.findSourcePermanentIndex(ctx);
        if (sourceIndex < 0 || !target.getBlockingTargets().contains(sourceIndex)) {
            throw new IllegalStateException("Target must be a creature blocking this creature");
        }
    }

    @ValidatesTarget(DestroyAttachmentsOnTargetCreatureEffect.class)
    public void validateDestroyAttachmentsOnTargetCreature(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DestroyTargetPermanentEffect.class)
    public void validateDestroyTargetPermanent(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    // Covers the whole destroy-plus-value family (DestroyTargetPermanentThenEffect). All members are
    // single-battlefield-target with protection honoured; the type restriction is the card's own
    // target filter. FoolishFate wraps it in a ConditionalReplacementEffect, whose base effect
    // (DestroyTargetPermanentEffect) carries the validation instead — checkEffectTargets unwraps to
    // the base — so this validator does not run for that card.
    @ValidatesTarget(DestroyTargetPermanentThenEffect.class)
    public void validateDestroyTargetPermanentThen(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(SacrificeTargetThenRevealUntilTypeToBattlefieldEffect.class)
    public void validateSacrificeTargetThenRevealUntilType(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DestroyTargetThenRevealUntilTypeToBattlefieldEffect.class)
    public void validateDestroyTargetThenRevealUntilType(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    // ===== Land destruction (Cryoclasm, Melt Terrain, Field of Ruin) =====

    @ValidatesTarget(DestroyTargetLandAndDamageControllerEffect.class)
    public void validateDestroyTargetLandAndDamageController(TargetValidationContext ctx) {
        requireLandTarget(ctx);
    }

    @ValidatesTarget(DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect.class)
    public void validateDestroyTargetAndEachPlayerSearchesBasicLand(TargetValidationContext ctx) {
        requireLandTarget(ctx);
    }

    // ===== Any-permanent destruction / sacrifice (Ghost Quarter & Erode target creature/pw/land,
    // Stone Giant a creature, Lowland Oaf a creature — the card/ability filter narrows further) =====

    @ValidatesTarget(DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect.class)
    public void validateDestroyTargetPermanentAndControllerSearchesLibrary(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DestroyTargetPermanentAtEndStepEffect.class)
    public void validateDestroyTargetPermanentAtEndStep(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(SacrificeTargetPermanentAtEndStepEffect.class)
    public void validateSacrificeTargetPermanentAtEndStep(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect.class)
    public void validateSacrificeTargetCreatureThenCreateTokens(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
        tvs.checkProtection(ctx, target);
    }

    private void requireLandTarget(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        if (!target.getCard().hasType(CardType.LAND)) {
            throw new IllegalStateException("Target must be a land");
        }
        tvs.checkProtection(ctx, target);
    }
}
