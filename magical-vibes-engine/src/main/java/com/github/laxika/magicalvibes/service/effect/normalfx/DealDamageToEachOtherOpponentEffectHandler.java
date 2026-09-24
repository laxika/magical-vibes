package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves combat-damage triggers that damage every other opponent separately. */
@Component
@RequiredArgsConstructor
public class DealDamageToEachOtherOpponentEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachOtherOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEachOtherOpponentEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (controllerId == null || damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = Math.max(0, amountEvaluationService.evaluate(
                gameData, e.amount(), AmountContext.forStackEntry(entry, source)));
        if (amount == 0) {
            return;
        }

        UUID damagedOpponentId = entry.getTargetId();
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(controllerId) || opponentId.equals(damagedOpponentId)) {
                continue;
            }
            int damage = gameQueryService.applyDamageMultiplier(gameData, amount, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, opponentId, damage);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
