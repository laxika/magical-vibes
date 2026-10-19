package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerThenGainLifeEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetPlayerOrPlaneswalkerThenGainLifeEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetPlayerOrPlaneswalkerThenGainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetPlayerOrPlaneswalkerThenGainLifeEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        int lifeBefore = gameData.playerLifeTotals.getOrDefault(targetId, 0);
        int dealtToPermanent = damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, damage, false);
        // Player damage is not reported back by the damage pipeline, so the life actually lost is the amount dealt.
        int damageDealt = targetIsPlayer
                ? Math.max(0, lifeBefore - gameData.playerLifeTotals.getOrDefault(targetId, 0))
                : dealtToPermanent;
        gameOutcomeService.checkWinCondition(gameData);

        if (damageDealt > 0) {
            lifeSupport.applyGainLife(gameData, entry.getControllerId(), damageDealt, null,
                    entry.getCard(), entry.getEntryType());
        }
    }
}
