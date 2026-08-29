package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDamageSourceControllerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves damage to the current controller of the source that caused a trigger. */
@Component
@RequiredArgsConstructor
public class DealDamageToDamageSourceControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToDamageSourceControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToDamageSourceControllerEffect) effect;
        if (damageEffect.amount() <= 0) return;

        UUID controllerId = damageEffect.damageSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentController(gameData, damageEffect.damageSourcePermanentId());
        if (controllerId == null) {
            controllerId = damageEffect.damageSourceControllerId();
        }
        if (controllerId == null || !gameData.playerIds.contains(controllerId)
                || damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, damageEffect.amount(), entry);
        damageSupport.dealDamageToPlayer(gameData, entry, controllerId, damage);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
