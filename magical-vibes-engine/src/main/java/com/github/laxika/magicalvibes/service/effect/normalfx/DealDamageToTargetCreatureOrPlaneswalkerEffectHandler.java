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
        int toughnessBefore = targetIsCreature ? gameQueryService.getEffectiveToughness(gameData, singleTarget) : 0;
        int markedDamageBefore = singleTarget == null ? 0 : singleTarget.getMarkedDamage();
        int loyaltyBefore = targetIsPlaneswalker ? singleTarget.getCounterCount(CounterType.LOYALTY) : 0;
        boolean sourceHasDeathtouch = tracksExcess
                && gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.DEATHTOUCH);

        // Multi-target / optional "up to N" ETB path: targets land on targetIds with targetId null.
        // When this effect is bound to a target group, narrow the flat list to that group so a
        // modal spell does not apply the same effect to targets belonging to another effect.
        List<UUID> effectTargets = entry.targetsForEffect(e);
        if (effectTargets != null && !effectTargets.isEmpty()
                && (effectTargets.size() > 1 || entry.getTargetId() == null)) {
            for (UUID targetId : effectTargets) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) continue;
                markForExileInsteadOfDying(gameData, target, e);
                if (!damageSupport.isDamagePreventedForCreature(gameData, entry, target)) {
                    damageSupport.dealCreatureDamage(gameData, entry, target, damage);
                }
            }
            return;
        }

        if (singleTarget != null) {
            markForExileInsteadOfDying(gameData, singleTarget, e);
        }
        int damageDealt = damageSupport.resolveCreatureTargetDamage(gameData, entry, damage);
        if (tracksExcess) {
            entry.setEventValue(singleTarget == null
                    ? 0
                    : damageSupport.computeExcessDamageToAnyTarget(
                    damageDealt, targetIsCreature, toughnessBefore, markedDamageBefore, sourceHasDeathtouch,
                    targetIsPlaneswalker, loyaltyBefore, false, 0));
        }
    }

    private boolean referencesExcessDamage(CardEffect effect) {
        return effect instanceof ConditionalEffect conditional
                && (conditional.condition() instanceof EventValueAtLeast
                || referencesExcessDamage(conditional.wrapped()));
    }

    private void markForExileInsteadOfDying(GameData gameData, Permanent target,
                                            DealDamageToTargetCreatureOrPlaneswalkerEffect effect) {
        if (effect.exileInsteadOfDie()
                && (gameQueryService.isCreature(gameData, target)
                || gameQueryService.isPlaneswalker(gameData, target))) {
            target.setExileInsteadOfDieThisTurn(true);
        }
    }
}
