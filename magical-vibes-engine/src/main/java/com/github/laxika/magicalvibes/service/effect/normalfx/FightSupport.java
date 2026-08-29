package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
        dealMutualDamage(gameData, entry, first, second, true);
        dealMutualDamage(gameData, entry, second, first, true);
    }

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
        int baseDamage = usePower
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
        int damage = gameQueryService.applyDamageMultiplier(gameData, baseDamage, entry);
        int markedDamageBefore = recipient.getMarkedDamage();
        int damageDealt = damageSupport.dealCreatureDamage(gameData, entry, recipient, damage, source);
        UUID recipientControllerId = gameQueryService.findPermanentController(gameData, recipient.getId());
        if (recipientControllerId != null
                && !recipientControllerId.equals(entry.getControllerId())
                && referencesExcessDamage(entry)) {
            boolean deathtouch = gameQueryService.sourceHasKeyword(
                    gameData, entry, source, Keyword.DEATHTOUCH);
            entry.setEventValue(damageSupport.computeExcessDamageToCreature(
                    gameData, recipient, damageDealt, markedDamageBefore, deathtouch));
        }
    }

    private boolean referencesExcessDamage(StackEntry entry) {
        return entry.getEffectsToResolve().stream()
                .anyMatch(this::referencesExcessDamage);
    }

    private boolean referencesExcessDamage(CardEffect effect) {
        return effect instanceof ConditionalEffect conditional
                && conditional.condition() instanceof EventValueAtLeast;
    }
}
