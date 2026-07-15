package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FightTargetsEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FightTargetsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (FightTargetsEffect) effect;

        List<UUID> firstGroup = entry.targetsForGroup(e.firstTargetGroup());
        List<UUID> secondGroup = entry.targetsForGroup(e.secondTargetGroup());
        if (firstGroup.isEmpty() || secondGroup.isEmpty()) {
            return; // Optional target not chosen ("up to one") — no fight happens
        }

        Permanent first = gameQueryService.findPermanentById(gameData, firstGroup.getFirst());
        Permanent second = gameQueryService.findPermanentById(gameData, secondGroup.getFirst());
        if (first == null || second == null) {
            return;
        }

        // First creature deals damage equal to its power to second creature
        int firstPower = gameQueryService.getPowerBasedDamage(gameData, first);
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.isPreventedFromDealingDamage(gameData, first)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(first.getCard().getName() + "'s damage is prevented."));
        } else if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, second, first)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(second.getCard().getName() + " has protection — damage from " + first.getCard().getName() + " prevented."));
        } else {
            int damage = gameQueryService.applyDamageMultiplier(gameData, firstPower, entry);
            damageSupport.dealDamageAndDestroyIfLethal(gameData, entry, second, damage, first);
        }

        // Second creature deals damage equal to its power to first creature
        int secondPower = gameQueryService.getPowerBasedDamage(gameData, second);
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.isPreventedFromDealingDamage(gameData, second)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(second.getCard().getName() + "'s damage is prevented."));
        } else if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, first, second)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(first.getCard().getName() + " has protection — damage from " + second.getCard().getName() + " prevented."));
        } else {
            int damage = gameQueryService.applyDamageMultiplier(gameData, secondPower, entry);
            damageSupport.dealDamageAndDestroyIfLethal(gameData, entry, first, damage, second);
        }
    }
}
