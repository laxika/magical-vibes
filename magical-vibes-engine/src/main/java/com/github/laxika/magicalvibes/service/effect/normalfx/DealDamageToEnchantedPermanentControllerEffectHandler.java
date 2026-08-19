package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPermanentControllerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves damage dealt by an Aura to the controller of its enchanted permanent. */
@Component
@RequiredArgsConstructor
public class DealDamageToEnchantedPermanentControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEnchantedPermanentControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEnchantedPermanentControllerEffect) effect;
        UUID controllerId = findEnchantedPermanentController(gameData, entry);
        if (controllerId == null || damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(), AmountContext.forStackEntry(entry, source));
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, amount, entry);
        damageSupport.dealDamageToPlayer(gameData, entry, controllerId, rawDamage);
        gameOutcomeService.checkWinCondition(gameData);
    }

    private UUID findEnchantedPermanentController(GameData gameData, StackEntry entry) {
        Permanent aura = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            aura = entry.getSourcePermanentSnapshot();
        }
        if (aura == null || aura.getAttachedTo() == null) {
            return null;
        }

        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted != null) {
            return gameQueryService.findPermanentController(gameData, enchanted.getId());
        }

        UUID capturedControllerId = entry.getTargetId();
        return gameData.playerIds.contains(capturedControllerId) ? capturedControllerId : null;
    }
}
