package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToOwnerOfTargetHauntedCreatureEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Deals the Kaya emblem's damage to the targeted creature's owner. */
@Component
@RequiredArgsConstructor
public class DealDamageToOwnerOfTargetHauntedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToOwnerOfTargetHauntedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToOwnerOfTargetHauntedCreatureEffect) effect;
        UUID targetId = targetId(entry, effect);
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        UUID ownerId = target.getCard().getOwnerId() != null
                ? target.getCard().getOwnerId()
                : gameQueryService.findPermanentController(gameData, target.getId());
        if (ownerId == null || !gameData.playerIds.contains(ownerId)
                || damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int amount = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        damageSupport.dealDamageToPlayer(gameData, entry, ownerId,
                gameQueryService.applyDamageMultiplier(gameData, amount, entry));
        gameOutcomeService.checkWinCondition(gameData);
    }

    private UUID targetId(StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.targetsForEffect(effect);
        return targets.isEmpty() ? entry.getTargetId() : targets.getFirst();
    }
}
