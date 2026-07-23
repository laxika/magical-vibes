package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SoulBurnEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link SoulBurnEffect}: deal X damage, then gain life equal to the unprevented damage
 * dealt, not exceeding black mana spent on X or the target's pre-damage life/loyalty/toughness.
 */
@Component
@RequiredArgsConstructor
public class SoulBurnEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SoulBurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        int x = entry.getXValue();
        int blackOnX = gameData.getSpellCastManaSpentOnX(entry.getCard().getId(), ManaColor.BLACK);

        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (!targetIsPlayer && targetPermanent == null) {
            return;
        }

        int targetCap;
        int damageBefore;
        if (targetIsPlayer) {
            targetCap = Math.max(0, gameData.getLife(targetId));
            damageBefore = gameData.damageDealtToPlayersThisTurn.getOrDefault(targetId, 0);
        } else if (targetPermanent.getCard().hasType(CardType.PLANESWALKER)) {
            targetCap = Math.max(0, targetPermanent.getCounterCount(CounterType.LOYALTY));
            damageBefore = targetPermanent.getCounterCount(CounterType.LOYALTY);
        } else {
            targetCap = Math.max(0, gameQueryService.getEffectiveToughness(gameData, targetPermanent));
            damageBefore = targetPermanent.getMarkedDamage();
        }

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, x, entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);

        int damageDealt;
        if (targetIsPlayer) {
            int damageAfter = gameData.damageDealtToPlayersThisTurn.getOrDefault(targetId, 0);
            damageDealt = Math.max(0, damageAfter - damageBefore);
        } else {
            Permanent after = gameQueryService.findPermanentById(gameData, targetId);
            if (after == null) {
                // Creature left the battlefield (unlikely mid-resolution before SBAs) — use cap.
                damageDealt = targetCap;
            } else if (after.getCard().hasType(CardType.PLANESWALKER)) {
                int loyaltyAfter = after.getCounterCount(CounterType.LOYALTY);
                damageDealt = Math.max(0, damageBefore - loyaltyAfter);
            } else {
                damageDealt = Math.max(0, after.getMarkedDamage() - damageBefore);
            }
        }

        int lifeGain = Math.min(damageDealt, Math.min(blackOnX, targetCap));
        if (lifeGain > 0) {
            lifeSupport.applyGainLife(gameData, entry.getControllerId(), lifeGain);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
