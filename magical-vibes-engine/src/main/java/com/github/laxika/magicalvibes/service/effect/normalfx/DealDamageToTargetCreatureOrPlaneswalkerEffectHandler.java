package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureOrPlaneswalkerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureOrPlaneswalkerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetCreatureOrPlaneswalkerEffect) effect;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int evaluated = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        int damage = gameQueryService.applyDamageMultiplier(gameData, evaluated, entry);

        boolean tracksExcess = entry.getEffectsToResolve().stream().anyMatch(this::referencesExcessDamage);
        Permanent singleTarget = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        boolean targetIsCreature = singleTarget != null && gameQueryService.isCreature(gameData, singleTarget);
        boolean targetIsPlaneswalker = singleTarget != null && singleTarget.getCard().hasType(CardType.PLANESWALKER);
        int lethalDamageThresholdBefore = targetIsCreature
                ? gameQueryService.getLethalDamageThreshold(gameData, singleTarget) : 0;
        int markedDamageBefore = singleTarget == null ? 0 : singleTarget.getMarkedDamage();
        int loyaltyBefore = targetIsPlaneswalker ? singleTarget.getCounterCount(CounterType.LOYALTY) : 0;
        boolean sourceHasDeathtouch = tracksExcess
                && gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.DEATHTOUCH);

        // Multi-target / optional "up to N" ETB path: targets land on targetIds with targetId null.
        // When this effect is bound to a target group, narrow the flat list to that group so a
        // modal spell does not apply the same effect to targets belonging to another effect.
        Map<UUID, Integer> damageBefore = e.exileInsteadOfDie()
                ? new HashMap<>(gameData.damageDealtToPermanentsThisTurn) : Map.of();
        List<UUID> effectTargets = entry.targetsForEffect(e);
        if (effectTargets != null && !effectTargets.isEmpty()
                && (effectTargets.size() > 1 || entry.getTargetId() == null)) {
            for (UUID targetId : effectTargets) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) continue;
                if (!damageSupport.isDamagePreventedForCreature(gameData, entry, target)) {
                    damageSupport.dealCreatureDamage(gameData, entry, target, damage);
                }
            }
            markDamagedPermanentsForExile(gameData, e, damageBefore);
            return;
        }

        int damageDealt = damageSupport.resolveCreatureTargetDamage(gameData, entry, damage);
        markDamagedPermanentsForExile(gameData, e, damageBefore);
        if (tracksExcess) {
            entry.setEventValue(singleTarget == null
                    ? 0
                    : damageSupport.computeExcessDamageToAnyTarget(
                    damageDealt, targetIsCreature, lethalDamageThresholdBefore, markedDamageBefore, sourceHasDeathtouch,
                    targetIsPlaneswalker, loyaltyBefore, false, 0));
        }
    }

    private boolean referencesExcessDamage(CardEffect effect) {
        return effect instanceof ConditionalEffect conditional
                && (conditional.condition() instanceof EventValueAtLeast
                || referencesExcessDamage(conditional.wrapped()));
    }

    private void markDamagedPermanentsForExile(GameData gameData,
            DealDamageToTargetCreatureOrPlaneswalkerEffect effect, Map<UUID, Integer> damageBefore) {
        if (!effect.exileInsteadOfDie()) return;
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (gameData.damageDealtToPermanentsThisTurn.getOrDefault(permanent.getId(), 0)
                        > damageBefore.getOrDefault(permanent.getId(), 0)) {
                    permanent.setExileInsteadOfDieThisTurn(true);
                }
            }
        }
    }
}
