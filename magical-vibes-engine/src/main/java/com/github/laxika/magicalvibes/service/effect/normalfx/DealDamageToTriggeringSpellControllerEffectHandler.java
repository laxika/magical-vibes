package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringSpellControllerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves damage to the controller of the spell or ability that caused a trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DealDamageToTriggeringSpellControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTriggeringSpellControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToTriggeringSpellControllerEffect) effect;
        var triggeringControllerId = entry.getTriggeringPermanentControllerId();
        var triggeringCardId = entry.getTriggeringCardId();
        if (triggeringControllerId == null && triggeringCardId != null) {
            for (StackEntry stackEntry : gameData.stack) {
                if (stackEntry.getTargetableId().equals(triggeringCardId)) {
                    triggeringControllerId = stackEntry.getControllerId();
                    break;
                }
            }
        }

        if (triggeringControllerId == null) {
            log.info("Game {} - Controller of triggering spell or ability is unavailable for damage", gameData.id);
            return;
        }

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)
                && damageEffect.amount() > 0) {
            int damage = gameQueryService.applyDamageMultiplier(gameData, damageEffect.amount(), entry);
            damageSupport.dealDamageToPlayer(gameData, entry, triggeringControllerId, damage);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
