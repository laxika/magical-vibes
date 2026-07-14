package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEqualToChosenTypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndDealDamageByHighestManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetControllerIfTargetHasKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToChosenTypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkerDealDamageAndReceivePowerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectTargetCreatureDamageFromChosenSourceToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectTargetCreatureNextDamageFromChosenSourceToControllerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DamageTargetValidators {

    private final TargetValidationService tvs;
    private final GameQueryService gameQueryService;

    @ValidatesTarget(DealDamageToTargetCreatureEffect.class)
    public void validateDealDamageToTargetCreature(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDamageToTargetCreatureOrPlaneswalkerEffect.class)
    public void validateDealDamageToTargetCreatureOrPlaneswalker(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        boolean validPermanentType = gameQueryService.isCreature(ctx.gameData(), target)
                || target.getCard().hasType(CardType.PLANESWALKER);
        if (!validPermanentType) {
            throw new IllegalStateException("Target must be a creature or planeswalker");
        }
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDamageToTargetOpponentOrPlaneswalkerEffect.class)
    public void validateDealDamageToTargetOpponentOrPlaneswalker(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        if (ctx.gameData().playerIds.contains(ctx.targetId())) {
            // Player target — must be an opponent (not the controller)
            java.util.UUID controllerId = tvs.findSourcePermanentController(ctx);
            if (controllerId != null && controllerId.equals(ctx.targetId())) {
                throw new IllegalStateException("Target must be an opponent");
            }
            return;
        }
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        if (!target.getCard().hasType(CardType.PLANESWALKER)) {
            throw new IllegalStateException("Target must be an opponent or planeswalker");
        }
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDamageToTargetPlayerOrPlaneswalkerEffect.class)
    public void validateDealDamageToTargetPlayerOrPlaneswalker(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        if (ctx.gameData().playerIds.contains(ctx.targetId())) {
            // Any player is a legal target (including the controller).
            return;
        }
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        if (!target.getCard().hasType(CardType.PLANESWALKER)) {
            throw new IllegalStateException("Target must be a player or planeswalker");
        }
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDamageToAnyTargetEffect.class)
    public void validateDealDamageToAnyTarget(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        if (ctx.gameData().playerIds.contains(ctx.targetId())) {
            return;
        }
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        boolean validPermanentType = gameQueryService.isCreature(ctx.gameData(), target)
                || target.getCard().hasType(CardType.PLANESWALKER);
        if (!validPermanentType) {
            throw new IllegalStateException("Target must be a creature, planeswalker, or player");
        }
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDamageToTargetAndTheirCreaturesEffect.class)
    public void validateDealDamageToTargetAndTheirCreatures(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        if (ctx.gameData().playerIds.contains(ctx.targetId())) {
            return;
        }
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        if (!target.getCard().hasType(CardType.PLANESWALKER)) {
            throw new IllegalStateException("Target must be a player or planeswalker");
        }
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect.class)
    public void validateDealDamageToAnyTargetEqualToSubtypeCountAndGainLife(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        if (ctx.gameData().playerIds.contains(ctx.targetId())) {
            return;
        }
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        boolean validPermanentType = gameQueryService.isCreature(ctx.gameData(), target)
                || target.getCard().hasType(CardType.PLANESWALKER);
        if (!validPermanentType) {
            throw new IllegalStateException("Target must be a creature, planeswalker, or player");
        }
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDividedDamageEffect.class)
    public void validateDealDividedDamage(TargetValidationContext ctx, DealDividedDamageEffect effect) {
        if (effect.etbAssignments()) {
            // Targets come from GameData.pendingETBDamageAssignments, outside the targeting pipeline.
            return;
        }
        if (ctx.targetId() == null) {
            // CHOSEN-mode casts/activations carry their targets in the damage-assignments map
            // (validated by SpellCastingService/the ability pipeline), not in targetId.
            return;
        }
        if (ctx.gameData().playerIds.contains(ctx.targetId())) {
            if (!effect.canTargetPlayers()) {
                throw new IllegalStateException("This spell cannot target players");
            }
            return;
        }
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        boolean validPermanentType = gameQueryService.isCreature(ctx.gameData(), target)
                || target.getCard().hasType(CardType.PLANESWALKER);
        if (!validPermanentType) {
            throw new IllegalStateException("Target must be a creature, planeswalker, or player");
        }
        tvs.checkProtection(ctx, target);
    }

    @ValidatesTarget(DealDamageToPlayersEffect.class)
    public void validateDealDamageToPlayers(TargetValidationContext ctx, DealDamageToPlayersEffect effect) {
        // Only the TARGET_PLAYER recipient chooses a player target; every other recipient
        // (CONTROLLER, EACH_*, ENCHANTED_PLAYER, the two permanent-controller riders) takes no
        // player target and must not trip the class-keyed validator, which runs unconditionally.
        if (effect.recipient() == DamageRecipient.TARGET_PLAYER) {
            tvs.requireTargetPlayer(ctx);
        }
    }

    // ===== "Any target" burn (creature / planeswalker / player), mirrors DealDamageToAnyTarget =====

    @ValidatesTarget(DealDamageToAnyTargetAndGainLifeEffect.class)
    public void validateDealDamageToAnyTargetAndGainLife(TargetValidationContext ctx) {
        validateAnyDamageTarget(ctx);
    }

    @ValidatesTarget(DealDamageToAnyTargetEqualToChosenTypeCountEffect.class)
    public void validateDealDamageToAnyTargetEqualToChosenTypeCount(TargetValidationContext ctx) {
        validateAnyDamageTarget(ctx);
    }

    @ValidatesTarget(DealXDamageToAnyTargetAndGainXLifeEffect.class)
    public void validateDealXDamageToAnyTargetAndGainXLife(TargetValidationContext ctx) {
        validateAnyDamageTarget(ctx);
    }

    @ValidatesTarget(MillControllerAndDealDamageByHighestManaValueEffect.class)
    public void validateMillControllerAndDealDamageByHighestManaValue(TargetValidationContext ctx) {
        validateAnyDamageTarget(ctx);
    }

    // ===== "Target creature" burn / fight / one-sided damage =====

    @ValidatesTarget(DealDamageToTargetControllerIfTargetHasKeywordEffect.class)
    public void validateDealDamageToTargetControllerIfTargetHasKeyword(TargetValidationContext ctx) {
        validateCreatureDamageTarget(ctx);
    }

    @ValidatesTarget(DealDamageToTargetCreatureEqualToChosenTypeCountEffect.class)
    public void validateDealDamageToTargetCreatureEqualToChosenTypeCount(TargetValidationContext ctx) {
        validateCreatureDamageTarget(ctx);
    }

    @ValidatesTarget(PlaneswalkerDealDamageAndReceivePowerDamageEffect.class)
    public void validatePlaneswalkerDealDamageAndReceivePowerDamage(TargetValidationContext ctx) {
        validateCreatureDamageTarget(ctx);
    }

    @ValidatesTarget(TargetCreatureDealsPowerDamageToSelfEffect.class)
    public void validateTargetCreatureDealsPowerDamageToSelf(TargetValidationContext ctx) {
        validateCreatureDamageTarget(ctx);
    }

    @ValidatesTarget(TargetCreatureDealsPowerDamageToControllerEffect.class)
    public void validateTargetCreatureDealsPowerDamageToController(TargetValidationContext ctx) {
        validateCreatureDamageTarget(ctx);
    }

    @ValidatesTarget(SourceFightsTargetCreatureEffect.class)
    public void validateSourceFightsTargetCreature(TargetValidationContext ctx) {
        validateCreatureDamageTarget(ctx);
    }

    // ===== Damage redirection onto a target creature (benign / neutral: no spell-color protection
    // check, matching the benign creature-mod validators) =====

    @ValidatesTarget(RedirectNextDamageToTargetCreatureEffect.class)
    public void validateRedirectNextDamageToTargetCreature(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    @ValidatesTarget(RedirectTargetCreatureDamageFromChosenSourceToSelfEffect.class)
    public void validateRedirectTargetCreatureDamageFromChosenSourceToSelf(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    @ValidatesTarget(RedirectTargetCreatureNextDamageFromChosenSourceToControllerEffect.class)
    public void validateRedirectTargetCreatureNextDamageFromChosenSourceToController(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
    }

    private void validateAnyDamageTarget(TargetValidationContext ctx) {
        tvs.requireTarget(ctx);
        if (ctx.gameData().playerIds.contains(ctx.targetId())) {
            return;
        }
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        boolean validPermanentType = gameQueryService.isCreature(ctx.gameData(), target)
                || target.getCard().hasType(CardType.PLANESWALKER);
        if (!validPermanentType) {
            throw new IllegalStateException("Target must be a creature, planeswalker, or player");
        }
        tvs.checkProtection(ctx, target);
    }

    private void validateCreatureDamageTarget(TargetValidationContext ctx) {
        Permanent target = tvs.requireBattlefieldTarget(ctx);
        tvs.requireCreature(ctx, target);
        tvs.checkProtection(ctx, target);
    }
}
