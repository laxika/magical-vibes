package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
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

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target != null) {
            markForExileInsteadOfDying(gameData, target, e);
        }
        damageSupport.resolveCreatureTargetDamage(gameData, entry, damage);
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
