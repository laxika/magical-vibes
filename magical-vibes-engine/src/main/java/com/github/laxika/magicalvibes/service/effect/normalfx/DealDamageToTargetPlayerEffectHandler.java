package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetPlayerEffect) effect;

        UUID targetId = entry.getTargetId();
        if (!gameData.playerIds.contains(targetId)) return;

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
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
            damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);

    }
}
