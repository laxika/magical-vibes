package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreaturesCombatDamage;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreaturesCombatDamageEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Registers Tamiyo, Field Researcher's "until your next turn, whenever either of those creatures
 * deals combat damage, you draw a card" watch against the chosen creatures.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedWatchedCreaturesCombatDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedWatchedCreaturesCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedWatchedCreaturesCombatDamageEffect) effect;

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetIds() != null) {
            targetIds = entry.getTargetIds();
        }
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        // "Up to two target creatures": a target that already left the battlefield contributes
        // nothing, and if every chosen creature is gone there is no watch to register at all.
        Set<UUID> watched = new LinkedHashSet<>();
        for (UUID targetId : targetIds) {
            if (gameQueryService.findPermanentById(gameData, targetId) != null) {
                watched.add(targetId);
            }
        }
        if (watched.isEmpty()) {
            return;
        }

        gameData.queueDelayedAction(new DelayedWatchedCreaturesCombatDamage(
                watched, entry.getControllerId(), e.effects(), entry.getCard(),
                e.combatDamageToPlayerOnly(), e.untilEndOfTurn()));
        log.info("Game {} - {} watches {} creature(s) for combat damage until its controller's next turn",
                gameData.id, entry.getCard().getName(), watched.size());
    }
}
