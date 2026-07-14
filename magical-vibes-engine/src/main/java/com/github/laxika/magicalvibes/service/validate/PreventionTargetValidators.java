package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageByTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Target validators for damage-prevention / redirection spells and abilities. Prevention is benign,
 * so — matching the scope of the existing benign validators (boost, grant) — these enforce only the
 * legal target TYPE and do not add a spell-color protection check (the single-{@code targetId} spell
 * path still runs its own protection check independently). The one exception,
 * {@link PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect}, redirects damage ONTO its target
 * and so is treated as harmful like the burn validators.
 */
@Service
@RequiredArgsConstructor
public class PreventionTargetValidators {

    private final TargetValidationService tvs;
    private final GameQueryService gameQueryService;

    // "any target" prevention (creature / planeswalker / player), e.g. Bandage, Healing Grace.
    @ValidatesTarget(PreventDamageToTargetEffect.class)
    public void validatePreventDamageToTarget(TargetValidationContext ctx) {
        validateAnyTargetType(ctx);
    }

    @ValidatesTarget(PreventDamageToTargetFromChosenSourceEffect.class)
    public void validatePreventDamageToTargetFromChosenSource(TargetValidationContext ctx) {
        validateAnyTargetType(ctx);
    }

    // Harm's Way: redirects the damage ONTO any target — harmful, so protection is honoured.
    @ValidatesTarget(PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect.class)
    public void validatePreventDamageFromChosenSourceAndRedirectToAnyTarget(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        if (ctx.gameData().playerIds.contains(ctx.targetId())) {
            return;
        }
        Permanent target = requireCreatureOrPlaneswalker(ctx);
        tvs.checkProtection(ctx, target);
    }

    // "prevent all damage to/by target creature" (Redeem, Inquisitor's Snare).
    @ValidatesTarget(PreventAllDamageToTargetCreatureEffect.class)
    public void validatePreventAllDamageToTargetCreature(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    @ValidatesTarget(PreventAllDamageByTargetCreatureEffect.class)
    public void validatePreventAllDamageByTargetCreature(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    // Remedy: divided prevention among target creatures and/or players. Targets are announced onto
    // StackEntry.damageAssignments (like DealDividedDamageEffect CHOSEN mode), so targetId is null on
    // that path — tolerate it, mirroring DealDividedDamage's validator.
    @ValidatesTarget(PreventDividedDamageEffect.class)
    public void validatePreventDividedDamage(TargetValidationContext ctx) {
        if (ctx.targetId() == null) {
            return;
        }
        if (ctx.gameData().playerIds.contains(ctx.targetId())) {
            return;
        }
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    private void validateAnyTargetType(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        if (ctx.gameData().playerIds.contains(ctx.targetId())) {
            return;
        }
        requireCreatureOrPlaneswalker(ctx);
    }

    private Permanent requireCreatureOrPlaneswalker(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        boolean valid = gameQueryService.isCreature(ctx.gameData(), target)
                || target.getCard().hasType(CardType.PLANESWALKER);
        if (!valid) {
            throw new IllegalStateException("Target must be a creature, planeswalker, or player");
        }
        return target;
    }
}
