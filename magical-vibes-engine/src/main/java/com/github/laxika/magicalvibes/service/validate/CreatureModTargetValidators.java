package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromCardTypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentFromTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreatureModTargetValidators {

    private final TargetValidationService tvs;

    @ValidatesTarget(TapOrUntapTargetPermanentEffect.class)
    public void validateTapOrUntapTargetPermanent(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(TapPermanentsEffect.class)
    public void validateTapPermanents(TargetValidationContext ctx, TapPermanentsEffect effect) {
        // Only the single-target scope validates a chosen permanent; the other scopes
        // (SELF, ENCHANTED, TARGET_PLAYERS_PERMANENTS, ALL_CREATURES) don't target one.
        if (effect.scope() == TapUntapScope.TARGET) {
            tvs.requireBattlefieldTarget(ctx);
        }
    }

    @ValidatesTarget(UntapPermanentsEffect.class)
    public void validateUntapPermanents(TargetValidationContext ctx, UntapPermanentsEffect effect) {
        // Only the single-target scope validates a chosen permanent; ALL_TARGETS, SELF,
        // CONTROLLED, OTHER_CONTROLLED_CREATURES and ATTACKED_CREATURES don't target one.
        if (effect.scope() == TapUntapScope.TARGET) {
            tvs.requireBattlefieldTarget(ctx);
        }
    }

    @ValidatesTarget(BoostTargetCreatureEffect.class)
    public void validateBoostTargetCreature(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    @ValidatesTarget(StaticBoostEffect.class)
    public void validateStaticBoost(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    @ValidatesTarget(AttachedBoostEffect.class)
    public void validateAttachedBoost(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    @ValidatesTarget(CantBlockThisTurnEffect.class)
    public void validateCantBlockThisTurn(TargetValidationContext ctx, CantBlockThisTurnEffect effect) {
        // Only the single-target scope validates a chosen creature; the TARGET_PLAYERS_PERMANENTS
        // and ALL_CREATURES scopes don't target a permanent.
        if (effect.scope() == TapUntapScope.TARGET) {
            Permanent target = tvs.requireBattlefieldTarget(ctx);
            tvs.requireCreature(ctx, target);
        }
    }

    @ValidatesTarget(MustBlockSourceEffect.class)
    public void validateMustBlockSource(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    @ValidatesTarget(SwitchPowerToughnessEffect.class)
    public void validateSwitchPowerToughness(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    @ValidatesTarget(SetBasePowerToughnessEffect.class)
    public void validateSetBasePowerToughness(TargetValidationContext ctx, SetBasePowerToughnessEffect effect) {
        if (effect.scope() == GrantScope.TARGET) {
            Permanent target = tvs.requireBattlefieldTarget(ctx);
            tvs.requireCreature(ctx, target);
        }
    }

    @ValidatesTarget(GrantColorUntilEndOfTurnEffect.class)
    public void validateGrantColorUntilEndOfTurn(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(GrantProtectionFromCardTypeUntilEndOfTurnEffect.class)
    public void validateGrantProtectionFromCardTypeUntilEndOfTurn(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    @ValidatesTarget(UnattachEquipmentFromTargetPermanentsEffect.class)
    public void validateUnattachEquipmentFromTargetPermanents(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(AddCardTypeToTargetPermanentEffect.class)
    public void validateAddCardTypeToTargetPermanent(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(AnimatePermanentsEffect.class)
    public void validateAnimatePermanents(TargetValidationContext ctx, AnimatePermanentsEffect effect) {
        // Only the TARGET scope (Tezzeret, Waker, Awakener Druid) validates a chosen permanent;
        // the SELF/OWN_LANDS/OWN_PERMANENTS scopes don't target one.
        if (effect.scope() == GrantScope.TARGET) {
            tvs.requireBattlefieldTarget(ctx);
        }
    }
}
