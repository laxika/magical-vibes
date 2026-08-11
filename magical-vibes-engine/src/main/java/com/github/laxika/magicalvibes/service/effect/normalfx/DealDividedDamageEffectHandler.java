package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Autowired
    private AmountEvaluationService amountEvaluationService;
    @Autowired
    private InteractionHandlerRegistry interactionHandlerRegistry;
    @Autowired
    private PredicateEvaluationService predicateEvaluationService;

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
                    if (e.targetRestriction() != null) {
                        resolveResolutionTimeAssignments(gameData, entry, e);
                        return;
                    }
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

    private void resolveResolutionTimeAssignments(GameData gameData, StackEntry entry,
                                                  DealDividedDamageEffect effect) {
        List<UUID> targets = entry.targetsForEffect(effect);
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int total = amountEvaluationService.evaluate(gameData, effect.totalDamage(),
                AmountContext.forStackEntry(entry, source));
        Map<UUID, Integer> assignments = new LinkedHashMap<>();
        for (Map.Entry<UUID, Integer> assignment : gameData.pendingETBDamageAssignments.entrySet()) {
            if (targets.contains(assignment.getKey()) && assignment.getValue() > 0) {
                assignments.put(assignment.getKey(), assignment.getValue());
            }
        }

        UUID pendingTargetId = gameData.pendingDividedDamageTargetId;
        if (pendingTargetId != null) {
            Integer chosen = gameData.chosenXValue;
            gameData.chosenXValue = null;
            gameData.pendingDividedDamageTargetId = null;
            if (chosen == null || !targets.contains(pendingTargetId)) {
                gameData.pendingETBDamageAssignments = assignments;
                return;
            }
            assignments.put(pendingTargetId, chosen);
            gameData.pendingETBDamageAssignments = assignments;
        }

        int assignedTotal = assignments.values().stream().mapToInt(Integer::intValue).sum();
        if (targets.isEmpty() || total <= 0) {
            gameData.pendingETBDamageAssignments = Map.of();
            return;
        }

        if (assignments.keySet().containsAll(targets)) {
            gameData.pendingETBDamageAssignments = Map.of();
            if (assignedTotal == total) {
                dealToAssignments(gameData, entry, effect, assignments);
            }
            return;
        }

        int remainingTargets = (int) targets.stream().filter(id -> !assignments.containsKey(id)).count();
        int remainingDamage = total - assignedTotal;
        if (remainingDamage < remainingTargets) {
            gameData.pendingETBDamageAssignments = Map.of();
            return;
        }

        UUID nextTarget = targets.stream().filter(id -> !assignments.containsKey(id)).findFirst().orElseThrow();
        int min = remainingTargets == 1 ? remainingDamage : 1;
        int max = remainingDamage - remainingTargets + 1;
        gameData.pendingDividedDamageTargetId = nextTarget;
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                entry.getControllerId(), min, max,
                entry.getCard().getName() + "'s ability — Choose damage for the target creature.",
                entry.getCard().getName()));
    }

    private void dealToAssignments(GameData gameData, StackEntry entry, DealDividedDamageEffect e,
                                   Map<UUID, Integer> assignments) {
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        String cardName = entry.getCard().getName();
        

        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            UUID targetId = assignment.getKey();
            if (!entry.isAssignmentTargetLegal(targetId)) continue;

            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, assignment.getValue(), entry);

            boolean targetIsPlayer = gameData.playerIds.contains(targetId);
            Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

            if (!targetIsPlayer && targetPermanent == null) continue;

            // Divided damage is "any target" damage (CR 115.4). A permanent that is not a creature,
            // planeswalker or battle at resolution (e.g. an animated land that reverted) is an
            // illegal target and isn't affected (CR 608.2b) — never burn lands.
            if (!targetIsPlayer && !damageSupport.isAnyTargetDamageRecipient(gameData, targetPermanent)) {
                continue;
            }

            if (!targetIsPlayer && e.targetRestriction() != null && predicateEvaluationService != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, targetPermanent,
                    e.targetRestriction())) {
                continue;
            }

            if (targetIsPlayer) {
                damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
                if (e.damagedPlayersCantCastNoncreatureSpells() && rawDamage > 0) {
                    gameData.playersCantCastNoncreatureSpellsThisTurn.add(targetId);
                    gameLogService.append(gameData, GameLog.text(
                            gameData.playerIdToName.get(targetId) + " can't cast noncreature spells this turn."));
                }
            } else if (gameQueryService.isDamagePreventable(gameData)
                    && gameQueryService.hasProtectionFromSource(gameData, targetPermanent, entry.getCard())) {
                gameLogService.append(gameData, GameLog.textCardText(cardName + "'s damage to ", targetPermanent.getCard(), " is prevented."));
            } else {
                damageSupport.dealCreatureDamage(gameData, entry, targetPermanent, rawDamage);
                if (e.damagedCreaturesCantBlock() && rawDamage > 0) {
                    targetPermanent.setCantBlockThisTurn(true);
                    gameLogService.append(gameData, GameLog.cardThen(targetPermanent.getCard(), " can't block this turn."));
                }
                if (e.tapDamagedCreatures() && rawDamage > 0) {
                    targetPermanent.tap();
                    gameLogService.append(gameData, GameLog.cardThen(targetPermanent.getCard(), " becomes tapped."));
                }
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
