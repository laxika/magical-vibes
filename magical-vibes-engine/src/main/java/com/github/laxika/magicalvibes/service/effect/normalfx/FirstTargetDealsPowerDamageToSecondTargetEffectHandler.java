package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FirstTargetDealsPowerDamageToSecondTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.List;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardColor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FirstTargetDealsPowerDamageToSecondTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FirstTargetDealsPowerDamageToSecondTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.size() < 2) {
            return; // No second target — "up to one" chose zero
        }

        UUID biterId = targets.get(0);
        UUID targetId = targets.get(1);

        Permanent biter = gameQueryService.findPermanentById(gameData, biterId);
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (biter == null || target == null) {
            return;
        }

        // The biting creature deals the damage — check if it is prevented from dealing damage
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.isPreventedFromDealingDamage(gameData, biter)) {
            String logEntry = biter.getCard().getName() + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        // Use the biting creature's color for protection checks (not the spell's color)
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, target, biter)) {
            CardColor biterColor = biter.getEffectiveColor();
            String logEntry = target.getCard().getName() + " has protection from " + (biterColor != null ? biterColor.name().toLowerCase() : "source") + " — damage prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, biter);
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, entry);
        damageSupport.dealDamageAndDestroyIfLethal(gameData, entry, target, rawDamage, biter);
    
    }
}
