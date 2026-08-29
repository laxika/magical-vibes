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
 * Shared two-creature mutual damage resolution. Used by fight flows and by effects that use a
 * creature's toughness instead of its power.
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
        dealMutualDamage(gameData, entry, first, second, true);
        dealMutualDamage(gameData, entry, second, first, true);
    }

    /** Has two creatures deal damage equal to their respective toughnesses to each other. */
    public void dealToughnessDamageToEachOther(GameData gameData, StackEntry entry,
                                               Permanent first, Permanent second) {
        if (first == null || second == null) {
            return;
        }
        dealMutualDamage(gameData, entry, first, second, false);
        dealMutualDamage(gameData, entry, second, first, false);
    }

    private void dealMutualDamage(GameData gameData, StackEntry entry, Permanent source,
                                  Permanent recipient, boolean usePower) {
        int damage = usePower
                ? gameQueryService.getPowerBasedDamage(gameData, source)
                : Math.max(0, gameQueryService.getEffectiveToughness(gameData, source));
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.isPreventedFromDealingDamage(gameData, source)) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
            return;
        }
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, recipient, source)) {
            gameLogService.append(gameData, GameLog.cardTextCard(recipient.getCard(), " has protection — damage from ", source.getCard(), " prevented."));
            return;
        }
        int modifiedDamage = gameQueryService.applyDamageMultiplier(gameData, damage, entry);
        damageSupport.dealCreatureDamage(gameData, entry, recipient, modifiedDamage, source);
    }
}
