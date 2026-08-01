package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shared "two creatures fight" resolution (CR 701.14a): each of the two creatures deals damage
 * equal to its power to the other. Used by every fight flow — the targeted
 * {@code FightTargetsEffect} spell/ability path and the deferred Guild Feud upkeep flow, which
 * fights two creatures that were just put onto the battlefield.
 */
@Component
@RequiredArgsConstructor
public class FightSupport {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    /** Has {@code first} and {@code second} fight each other; either being gone is a no-op. */
    public void fight(GameData gameData, StackEntry entry, Permanent first, Permanent second) {
        if (first == null || second == null) {
            return;
        }
        dealFightDamage(gameData, entry, first, second);
        dealFightDamage(gameData, entry, second, first);
    }

    private void dealFightDamage(GameData gameData, StackEntry entry, Permanent source, Permanent recipient) {
        int power = gameQueryService.getPowerBasedDamage(gameData, source);
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.isPreventedFromDealingDamage(gameData, source)) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
            return;
        }
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, recipient, source)) {
            gameLogService.append(gameData, GameLog.cardTextCard(recipient.getCard(), " has protection — damage from ", source.getCard(), " prevented."));
            return;
        }
        int damage = gameQueryService.applyDamageMultiplier(gameData, power, entry);
        damageSupport.dealCreatureDamage(gameData, entry, recipient, damage, source);
    }
}
