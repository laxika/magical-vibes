package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToSelfEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetCreatureDealsPowerDamageToSelfEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureDealsPowerDamageToSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, target)) {
            String logEntry = target.getCard().getName() + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.hasProtectionFromSource(gameData, target, target)) {
            CardColor targetColor = target.getEffectiveColor();
            String logEntry = target.getCard().getName() + " has protection from "
                    + (targetColor != null ? targetColor.name().toLowerCase() : "source")
                    + " — damage prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, target);
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, entry);
        damageSupport.dealDamageAndDestroyIfLethal(gameData, entry, target, rawDamage, target);
    }
}
