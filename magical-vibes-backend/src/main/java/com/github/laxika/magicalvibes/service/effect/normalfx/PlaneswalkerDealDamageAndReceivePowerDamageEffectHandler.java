package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkerDealDamageAndReceivePowerDamageEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaneswalkerDealDamageAndReceivePowerDamageEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlaneswalkerDealDamageAndReceivePowerDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PlaneswalkerDealDamageAndReceivePowerDamageEffect) effect;

        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) return;

        String cardName = entry.getCard().getName();

        // Step 1: Planeswalker deals fixed damage to target creature
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
        if (rawDamage > 0) {
            if (!(gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.hasProtectionFromSource(gameData, target, entry.getCard()))) {
                if (damageSupport.dealCreatureDamage(gameData, entry, target, rawDamage)) {
                    gameData.pendingLethalDamageDestructions.add(target);
                }
                gameBroadcastService.logAndBroadcast(gameData,
                        cardName + " deals " + rawDamage + " damage to " + target.getCard().getName() + ".");
            } else {
                gameBroadcastService.logAndBroadcast(gameData,
                        cardName + "'s damage to " + target.getCard().getName() + " is prevented.");
            }
        }

        // Step 2: Target creature deals damage equal to its power to the source planeswalker
        // (removes loyalty counters)
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePlaneswalker = sourcePermanentId != null
                ? gameQueryService.findPermanentById(gameData, sourcePermanentId) : null;

        if (sourcePlaneswalker != null) {
            int targetPower = gameQueryService.getPowerBasedDamage(gameData, target);
            int newLoyalty = Math.max(0, sourcePlaneswalker.getCounterCount(CounterType.LOYALTY) - targetPower);
            sourcePlaneswalker.setCounterCount(CounterType.LOYALTY, newLoyalty);
            gameBroadcastService.logAndBroadcast(gameData,
                    target.getCard().getName() + " deals " + targetPower + " damage to " + cardName
                            + ". (" + cardName + " now has " + newLoyalty + " loyalty.)");
            log.info("Game {} - {} takes {} damage from {}, loyalty now {}",
                    gameData.id, cardName, targetPower, target.getCard().getName(), newLoyalty);
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
