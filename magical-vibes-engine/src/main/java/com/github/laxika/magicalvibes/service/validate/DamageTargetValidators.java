package com.github.laxika.magicalvibes.service.validate;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Escape-hatch target validators for damage effects — the ones whose legality depends on more than
 * target-type structure and so cannot be expressed by a declarative {@link
 * com.github.laxika.magicalvibes.model.effect.TargetSpec}. The purely structural damage validators
 * (any-target burn, target-creature burn/fight, damage redirection, creature-or-planeswalker /
 * player-or-planeswalker burn) were migrated to {@code targetSpec()} and their validators deleted.
 */
@Service
@RequiredArgsConstructor
public class DamageTargetValidators {

    private final TargetValidationService tvs;

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
        // Which permanents are legal is the declared target's job, not this validator's:
        // TargetValidationService interprets DealDividedDamageEffect.targetSpec() — "any target"
        // (CR 115.4) or a creature, narrowed by targetRestriction — before every registered
        // validator runs, and it evaluates that layer-aware (CR 613.1d). Re-checking the type here
        // only re-stated CR 115.4 from the *printed* type line, which layer 4 can change. All that
        // is left for the escape hatch is the zone.
        Permanent target = tvs.requireBattlefieldTarget(ctx);
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
}
