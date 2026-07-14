package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreaturePerChosenTypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherCreatureBecomesCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.MassFightTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PackHuntEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreaturePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromTargetAndBoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BuffTargetCreatureIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;
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

    @ValidatesTarget(BuffTargetCreatureIndefinitelyEffect.class)
    public void validateBuffTargetCreatureIndefinitely(TargetValidationContext ctx) {
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
    public void validateSwitchPowerToughness(TargetValidationContext ctx, SwitchPowerToughnessEffect effect) {
        if (effect.self()) {
            return;
        }
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

    @ValidatesTarget(BecomeChosenColorsUntilEndOfTurnEffect.class)
    public void validateBecomeChosenColorsUntilEndOfTurn(TargetValidationContext ctx) {
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

    // ===== Target-creature modifiers (benign / neutral: type only, no spell-color protection —
    // matching BoostTargetCreature above) =====

    @ValidatesTarget(BoostTargetCreaturePerChosenTypeCountEffect.class)
    public void validateBoostTargetCreaturePerChosenTypeCount(TargetValidationContext ctx) {
        requireCreatureTarget(ctx);
    }

    @ValidatesTarget(EachOtherCreatureBecomesCopyOfTargetCreatureUntilEndOfTurnEffect.class)
    public void validateEachOtherCreatureBecomesCopyOfTargetCreature(TargetValidationContext ctx) {
        requireCreatureTarget(ctx);
    }

    @ValidatesTarget(GrantEffectToTargetUntilEndOfTurnEffect.class)
    public void validateGrantEffectToTargetUntilEndOfTurn(TargetValidationContext ctx) {
        requireCreatureTarget(ctx);
    }

    @ValidatesTarget(GrantChosenKeywordToTargetEffect.class)
    public void validateGrantChosenKeywordToTarget(TargetValidationContext ctx) {
        requireCreatureTarget(ctx);
    }

    @ValidatesTarget(TargetCreatureBecomesSubtypeUntilEndOfTurnEffect.class)
    public void validateTargetCreatureBecomesSubtypeUntilEndOfTurn(TargetValidationContext ctx) {
        requireCreatureTarget(ctx);
    }

    @ValidatesTarget(MustAttackThisTurnEffect.class)
    public void validateMustAttackThisTurn(TargetValidationContext ctx) {
        requireCreatureTarget(ctx);
    }

    @ValidatesTarget(MustBeBlockedByAllCreaturesThisTurnEffect.class)
    public void validateMustBeBlockedByAllCreaturesThisTurn(TargetValidationContext ctx) {
        requireCreatureTarget(ctx);
    }

    @ValidatesTarget(MustBeBlockedIfAbleThisTurnEffect.class)
    public void validateMustBeBlockedIfAbleThisTurn(TargetValidationContext ctx) {
        requireCreatureTarget(ctx);
    }

    @ValidatesTarget(CantBlockSourceEffect.class)
    public void validateCantBlockSource(TargetValidationContext ctx) {
        requireCreatureTarget(ctx);
    }

    @ValidatesTarget(RemoveTargetFromCombatEffect.class)
    public void validateRemoveTargetFromCombat(TargetValidationContext ctx) {
        requireCreatureTarget(ctx);
    }

    @ValidatesTarget(MakeTargetCreaturePreparedEffect.class)
    public void validateMakeTargetCreaturePrepared(TargetValidationContext ctx) {
        requireCreatureTarget(ctx);
    }

    @ValidatesTarget(RemoveCounterFromTargetAndGainLifeEffect.class)
    public void validateRemoveCounterFromTargetAndGainLife(TargetValidationContext ctx) {
        requireCreatureTarget(ctx);
    }

    // Equip: "Attach to target creature you control" (control restriction rides on the ability filter).
    @ValidatesTarget(EquipEffect.class)
    public void validateEquip(TargetValidationContext ctx) {
        requireCreatureTarget(ctx);
    }

    // ===== Fights (harmful): a fight spell can't target a creature with protection from its color
    // at targeting time (CR 702.16e), so protection is honoured =====

    @ValidatesTarget(MassFightTargetCreatureEffect.class)
    public void validateMassFightTargetCreature(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(PackHuntEffect.class)
    public void validatePackHunt(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
        tvs.checkProtection(ctx, target);
    }

    // ===== Target-permanent modifiers (any permanent; the card/ability filter narrows further) =====

    @ValidatesTarget(GrantProtectionChoiceUntilEndOfTurnEffect.class)
    public void validateGrantProtectionChoiceUntilEndOfTurn(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(CreateTokenCopyOfTargetPermanentEffect.class)
    public void validateCreateTokenCopyOfTargetPermanent(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(SetChosenColorUntilEndOfTurnEffect.class)
    public void validateSetChosenColorUntilEndOfTurn(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(DoubleCountersOnTargetPermanentEffect.class)
    public void validateDoubleCountersOnTargetPermanent(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(RemoveChargeCountersFromTargetPermanentEffect.class)
    public void validateRemoveChargeCountersFromTargetPermanent(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    @ValidatesTarget(RemoveCountersFromTargetAndBoostSelfEffect.class)
    public void validateRemoveCountersFromTargetAndBoostSelf(TargetValidationContext ctx) {
        tvs.requireBattlefieldTarget(ctx);
    }

    // ===== Land-type granting (target land) =====

    @ValidatesTarget(GrantBasicLandTypeToTargetEffect.class)
    public void validateGrantBasicLandTypeToTarget(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        if (!target.getCard().hasType(CardType.LAND)) {
            throw new IllegalStateException("Target must be a land");
        }
    }

    private void requireCreatureTarget(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }
}
