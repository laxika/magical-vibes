package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerMaySacrificeOrDamageEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a targeted permanent controller's sacrifice-or-damage choice. */
@Component
@RequiredArgsConstructor
public class TargetPermanentControllerMaySacrificeOrDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPermanentControllerMaySacrificeOrDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPermanentControllerMaySacrificeOrDamageEffect) effect;
        Permanent target = target(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null) {
            return;
        }

        if (!canSacrifice(gameData, target, targetControllerId, entry.getControllerId())) {
            dealDamage(gameData, entry, e, target, targetControllerId);
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                targetControllerId,
                List.of(effect),
                "Sacrifice it?",
                target.getId(),
                null,
                entry.getSourcePermanentId(),
                null,
                0,
                0,
                null,
                null,
                null,
                entry.getSourcePermanentSnapshot(),
                entry.getControllerId(),
                null,
                entry.getEventValue()));
    }

    public void resolveChoice(GameData gameData, PendingMayAbility ability,
            boolean accepted, TargetPermanentControllerMaySacrificeOrDamageEffect effect) {
        Permanent target = target(gameData, ability.targetCardId());
        UUID targetControllerId = target == null
                ? null : gameQueryService.findPermanentController(gameData, target.getId());
        UUID sourceControllerId = ability.sourceControllerId() != null
                ? ability.sourceControllerId() : ability.controllerId();

        if (accepted && target != null && targetControllerId != null
                && targetControllerId.equals(ability.controllerId())
                && canSacrifice(gameData, target, targetControllerId, sourceControllerId)) {
            destructionSupport.sacrificeAndLog(gameData, target, targetControllerId);
        } else if (targetControllerId != null) {
            dealDamage(gameData, syntheticEntry(ability, sourceControllerId), effect,
                    target, targetControllerId);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private boolean canSacrifice(GameData gameData, Permanent target, UUID targetControllerId,
            UUID sourceControllerId) {
        return !gameQueryService.isLand(gameData, target)
                && gameQueryService.canEffectCauseSacrifice(gameData, targetControllerId, sourceControllerId)
                && !gameQueryService.cantBeSacrificed(gameData, target);
    }

    private Permanent target(GameData gameData, UUID targetId) {
        return targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
    }

    private void dealDamage(GameData gameData, StackEntry entry,
            TargetPermanentControllerMaySacrificeOrDamageEffect effect,
            Permanent target, UUID playerId) {
        StackEntry damageEntry = entry;
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (effect.targetIsDamageSource() && target != null) {
            damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    target.getCard(),
                    playerId,
                    target.getCard().getName() + "'s ability",
                    new ArrayList<>(),
                    target.getId(),
                    target.getId());
            source = target;
        }
        int damage = amountEvaluationService.evaluate(gameData, effect.damage(),
                AmountContext.forStackEntry(damageEntry, source));
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, damageEntry);
        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, damageEntry)) {
            damageSupport.dealDamageToPlayer(gameData, damageEntry, playerId, rawDamage);
        }
    }

    private StackEntry syntheticEntry(PendingMayAbility ability, UUID sourceControllerId) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                sourceControllerId,
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(),
                ability.controllerId(),
                ability.sourcePermanentId());
        entry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        return entry;
    }
}
