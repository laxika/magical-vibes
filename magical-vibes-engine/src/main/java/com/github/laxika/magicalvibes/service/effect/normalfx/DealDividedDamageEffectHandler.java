package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DealDividedDamageEffect} for every {@link com.github.laxika.magicalvibes.model.effect.DivisionMode}.
 *
 * <p>CHOSEN reads the controller-announced per-target amounts from {@code StackEntry.damageAssignments}
 * (or {@code GameData.pendingETBDamageAssignments} for the ETB path); EVEN splits the total evenly
 * across {@code StackEntry.targetIds}; ORDERED assigns the effect's fixed amounts by target order.
 * All three then run the same per-target damage loop that routes through {@link DamageSupport} so
 * prevention, protection and damage-multiplier shields keep working per target.
 */
@Component
@RequiredArgsConstructor
public class DealDividedDamageEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDividedDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDividedDamageEffect) effect;

        switch (e.mode()) {
            case CHOSEN -> {
                if (e.etbAssignments()) {
                    Map<UUID, Integer> assignments = gameData.pendingETBDamageAssignments;
                    gameData.pendingETBDamageAssignments = Map.of();
                    // dealDividedDamageToAnyTargets already calls checkWinCondition internally.
                    damageSupport.dealDividedDamageToAnyTargets(
                            gameData, entry.getCard(), entry.getControllerId(), assignments);
                    return;
                }
                Map<UUID, Integer> assignments = entry.getDamageAssignments();
                if (assignments == null || assignments.isEmpty()) return;
                dealToAssignments(gameData, entry, e, assignments);
            }
            case EVEN -> {
                List<UUID> targets = entry.getDeclaredTargetIds();
                boolean usesFlatTargets = !targets.isEmpty();
                if (targets.isEmpty()) {
                    if (entry.getTargetId() != null) {
                        targets = List.of(entry.getTargetId());
                    } else {
                        return;
                    }
                }
                int damagePerTarget = entry.getXValue() / targets.size();
                Map<UUID, Integer> assignments = new LinkedHashMap<>();
                for (int i = 0; i < targets.size(); i++) {
                    if (!usesFlatTargets || entry.isTargetLegal(i)) {
                        assignments.put(targets.get(i), damagePerTarget);
                    }
                }
                dealToAssignments(gameData, entry, e, assignments);
            }
            case ORDERED -> {
                List<UUID> targets = entry.getDeclaredTargetIds();
                List<Integer> amounts = e.orderedAmounts();
                Map<UUID, Integer> assignments = new LinkedHashMap<>();
                for (int i = 0; i < Math.min(targets.size(), amounts.size()); i++) {
                    if (entry.isTargetLegal(i)) {
                        assignments.put(targets.get(i), amounts.get(i));
                    }
                }
                dealToAssignments(gameData, entry, e, assignments);
            }
        }
    }

    private void dealToAssignments(GameData gameData, StackEntry entry, DealDividedDamageEffect e,
                                   Map<UUID, Integer> assignments) {
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        String cardName = entry.getCard().getName();
        List<Permanent> destroyed = new ArrayList<>();

        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            UUID targetId = assignment.getKey();
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, assignment.getValue(), entry);

            boolean targetIsPlayer = gameData.playerIds.contains(targetId);
            Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

            if (!targetIsPlayer && targetPermanent == null) continue;

            // Divided damage only ever targets creatures, planeswalkers, or players. A permanent
            // that is none of those at resolution (e.g. an animated land that reverted) is an
            // illegal target and isn't affected (CR 608.2b) — never burn lands.
            if (!targetIsPlayer
                    && !gameQueryService.isCreature(gameData, targetPermanent)
                    && !targetPermanent.getCard().hasType(CardType.PLANESWALKER)) {
                continue;
            }

            if (targetIsPlayer) {
                damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
            } else if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.hasProtectionFromSource(gameData, targetPermanent, entry.getCard())) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(cardName + "'s damage to ", targetPermanent.getCard(), " is prevented."));
            } else {
                damageSupport.dealCreatureDamage(gameData, entry, targetPermanent, rawDamage);
                if (e.damagedCreaturesCantBlock() && rawDamage > 0) {
                    targetPermanent.setCantBlockThisTurn(true);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(targetPermanent.getCard(), " can't block this turn."));
                }
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
