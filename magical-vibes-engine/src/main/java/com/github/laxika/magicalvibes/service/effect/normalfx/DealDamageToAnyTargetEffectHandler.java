package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToAnyTargetEffect) effect;

        // Group-aimed damage (e.g. Goblin Barrage's kicked "4 damage to target player or
        // planeswalker"): resolve against the declared target group's chosen target rather
        // than the entry's single target.
        UUID targetId = e.targetGroup() >= 0
                ? entry.targetsForGroup(e.targetGroup()).stream().findFirst().orElse(null)
                : entry.getTargetId();
        if (targetId == null) return;

        // Mark the target creature for exile-instead-of-die before dealing damage,
        // so that if lethal damage destroys it immediately, the replacement applies.
        if (e.exileInsteadOfDie()) {
            boolean targetIsPlayer = gameData.playerIds.contains(targetId);
            if (!targetIsPlayer) {
                Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
                if (targetPermanent != null && gameQueryService.isCreature(gameData, targetPermanent)) {
                    targetPermanent.setExileInsteadOfDieThisTurn(true);
                }
            }
        }

        // Source-relative amounts use the live source permanent when it is still on the
        // battlefield, else the last-known snapshot (e.g. sacrificed as an activation cost).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, entry);

        // "If X is 5 or more, … the damage can't be prevented" (Banefire): while the gate holds,
        // suppress every prevention path (all guarded by isDamagePreventable) for the duration of
        // this one damage event, then restore. Reuses the whole any-target damage pipeline.
        boolean unpreventable = e.unpreventableWhen() != null
                && conditionEvaluationService.isMet(gameData, e.unpreventableWhen(), ConditionContext.forStackEntry(entry));
        if (unpreventable) {
            boolean previous = gameData.damageCantBePreventedThisTurn;
            gameData.damageCantBePreventedThisTurn = true;
            try {
                damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, e.cantRegenerate());
            } finally {
                gameData.damageCantBePreventedThisTurn = previous;
            }
        } else {
            damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, e.cantRegenerate());
        }
        gameOutcomeService.checkWinCondition(gameData);

    }
}
