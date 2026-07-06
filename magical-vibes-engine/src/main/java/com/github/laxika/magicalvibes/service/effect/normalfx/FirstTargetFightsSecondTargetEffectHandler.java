package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FirstTargetFightsSecondTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.List;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FirstTargetFightsSecondTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FirstTargetFightsSecondTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.size() < 2) {
            return;
        }

        UUID firstId = targets.get(0);
        UUID secondId = targets.get(1);

        Permanent first = gameQueryService.findPermanentById(gameData, firstId);
        Permanent second = gameQueryService.findPermanentById(gameData, secondId);
        if (first == null || second == null) {
            return;
        }

        // First creature deals damage equal to its power to second creature
        int firstPower = gameQueryService.getPowerBasedDamage(gameData, first);
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.isPreventedFromDealingDamage(gameData, first)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    first.getCard().getName() + "'s damage is prevented.");
        } else if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, second, first)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    second.getCard().getName() + " has protection — damage from " + first.getCard().getName() + " prevented.");
        } else {
            int damage = gameQueryService.applyDamageMultiplier(gameData, firstPower, entry);
            damageSupport.dealDamageAndDestroyIfLethal(gameData, entry, second, damage, first);
        }

        // Second creature deals damage equal to its power to first creature
        int secondPower = gameQueryService.getPowerBasedDamage(gameData, second);
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.isPreventedFromDealingDamage(gameData, second)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    second.getCard().getName() + "'s damage is prevented.");
        } else if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, first, second)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    first.getCard().getName() + " has protection — damage from " + second.getCard().getName() + " prevented.");
        } else {
            int damage = gameQueryService.applyDamageMultiplier(gameData, secondPower, entry);
            damageSupport.dealDamageAndDestroyIfLethal(gameData, entry, first, damage, second);
        }
    
    }
}
