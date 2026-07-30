package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenDestroyEnchantedAtZeroEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveCounterFromSourceThenDestroyEnchantedAtZeroEffect} (Orcish Mine): removes one
 * counter from the source Aura and, once none are left, destroys the enchanted permanent and damages
 * its controller.
 */
@Component
@RequiredArgsConstructor
public class RemoveCounterFromSourceThenDestroyEnchantedAtZeroEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromSourceThenDestroyEnchantedAtZeroEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterFromSourceThenDestroyEnchantedAtZeroEffect) effect;

        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            return;
        }
        int current = aura.getCounterCount(e.counterType());
        if (current <= 0) {
            return;
        }
        aura.setCounterCount(e.counterType(), current - 1);
        gameLogService.append(gameData, GameLog.cardThen(aura.getCard(),
                " loses a " + permanentCounterSupport.counterTypeName(e.counterType()) + " counter."));

        if (current - 1 > 0 || !aura.isAttached()) {
            return;
        }
        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, enchanted.getId());
        destructionSupport.tryDestroyAndLog(gameData, enchanted, entry.getCard().getName());
        if (controllerId != null) {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damageToController(), entry);
            damageSupport.dealDamageToPlayer(gameData, entry, controllerId, rawDamage);
        }
    }
}
