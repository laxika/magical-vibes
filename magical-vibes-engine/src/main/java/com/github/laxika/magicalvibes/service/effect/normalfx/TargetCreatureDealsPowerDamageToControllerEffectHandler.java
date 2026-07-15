package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToControllerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetCreatureDealsPowerDamageToControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureDealsPowerDamageToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId == null) {
            return;
        }

        // The target creature is the damage source (CR 608.2h). If it is prevented from dealing
        // damage, nothing happens.
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, target)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(target.getCard().getName() + "'s damage is prevented."));
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, target);

        // Build a temporary entry whose source is the target creature, so prevention/protection/
        // lifelink and "deals damage" triggers key off the creature rather than Dong Zhou.
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                target.getCard(),
                controllerId,
                target.getCard().getName() + "'s ability",
                List.of(),
                null,
                target.getId());

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, damageEntry);
        damageSupport.dealDamageToPlayer(gameData, damageEntry, controllerId, rawDamage);
    }
}
