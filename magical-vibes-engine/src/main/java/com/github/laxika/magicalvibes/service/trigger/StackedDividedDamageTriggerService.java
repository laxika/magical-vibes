package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Announces the targets and division of divided triggered abilities that trigger sources put onto
 * the stack without one: "deals N damage divided as you choose among one, two, or three targets"
 * ({@link DealDividedDamageEffect} with {@code etbAssignments} and no target restriction — Inferno
 * Titan, Gang of Devils, Gandalf, Spark Starter) and "prevent the next N damage … to any number of
 * targets, divided as you choose" ({@link PreventDividedDamageEffect} with {@code etbAssignments} —
 * Angel of Salvation). As with a spell, the controller chooses one to {@code maxTargets} targets
 * and divides the amount, each target getting at least one, before any player receives priority
 * (CR 601.2c-d, 603.3d); an ability with no legal target is removed from the stack.
 *
 * <p>A division already staged in {@code GameData.pendingETBDamageAssignments} (a cast that
 * supplied it with the spell) is taken over as the announced division instead of prompting.</p>
 */
@Slf4j
@Service
public class StackedDividedDamageTriggerService {

    private final TriggerTargetCollector triggerTargetCollector;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final TriggerCollectionService triggerCollectionService;

    public StackedDividedDamageTriggerService(TriggerTargetCollector triggerTargetCollector,
                                              GameQueryService gameQueryService,
                                              GameLogService gameLogService,
                                              AmountEvaluationService amountEvaluationService,
                                              @Lazy PlayerInputService playerInputService,
                                              @Lazy InputCompletionService inputCompletionService,
                                              @Lazy TriggerCollectionService triggerCollectionService) {
        this.triggerTargetCollector = triggerTargetCollector;
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.amountEvaluationService = amountEvaluationService;
        this.playerInputService = playerInputService;
        this.inputCompletionService = inputCompletionService;
        this.triggerCollectionService = triggerCollectionService;
    }

    /**
     * Announces the division of every stack entry still waiting for one, from the bottom of the
     * stack up: a staged division is taken over directly, an entry with no legal target is removed,
     * and the first entry that needs its controller's choice begins the target choice.
     *
     * @return {@code true} if a target choice is now awaiting input
     */
    public boolean announceNextDivision(GameData gameData) {
        for (StackEntry entry : List.copyOf(gameData.stack)) {
            Division divided = unannouncedDivision(entry);
            if (divided == null) {
                continue;
            }
            if (!gameData.pendingETBDamageAssignments.isEmpty()) {
                Map<UUID, Integer> staged = new LinkedHashMap<>(gameData.pendingETBDamageAssignments);
                gameData.pendingETBDamageAssignments = Map.of();
                announce(gameData, entry, staged);
                continue;
            }
            Permanent source = sourceOf(gameData, entry);
            int total = amountEvaluationService.evaluate(gameData, divided.total(),
                    AmountContext.forStackEntry(entry, source));
            List<UUID> validTargets = total <= 0 ? List.of() : triggerTargetCollector.collect(
                    gameData, List.of(divided.targetingEquivalent()), null, entry.getControllerId(), entry.getCard(), TriggerTargetCollector.Options.ATTACK,
                    source).validTargets();
            if (validTargets.isEmpty()) {
                gameData.stack.remove(entry);
                gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                        "'s triggered ability has no legal targets and is removed from the stack."));
                log.info("Game {} - {} divided-damage trigger removed (no legal targets)",
                        gameData.id, entry.getCard().getName());
                continue;
            }
            List<UUID> validPlayers = validTargets.stream().filter(gameData.playerIds::contains).toList();
            List<UUID> validPermanents = validTargets.stream()
                    .filter(id -> !gameData.playerIds.contains(id))
                    .toList();
            int maxTargets = divided.maxTargets() > 0 ? Math.min(divided.maxTargets(), total) : total;
            maxTargets = Math.min(maxTargets, validTargets.size());
            playerInputService.beginMultiPermanentOrPlayerChoice(gameData, entry.getControllerId(),
                    validPermanents, validPlayers, maxTargets,
                    new MultiPermanentChoiceContext.StackedDividedDamageTargets(
                            entry.getTargetableId(), entry.getCard(), entry.getControllerId(), total,
                            divided.prevention()),
                    entry.getCard().getName() + "'s ability — Choose up to " + maxTargets
                            + " targets to divide " + total
                            + (divided.prevention() ? " damage prevention among." : " damage among."));
            log.info("Game {} - {} divided-damage trigger awaiting targets", gameData.id, entry.getCard().getName());
            return true;
        }
        return false;
    }

    /**
     * Records the chosen targets; a single target takes all the damage, while several targets go
     * on to the per-target division.
     */
    public void handleTargetsChosen(GameData gameData, List<UUID> targetIds,
                                    MultiPermanentChoiceContext.StackedDividedDamageTargets ctx) {
        if (targetIds.size() > ctx.total()) {
            throw new IllegalStateException("Each target must receive at least 1 of the divided amount");
        }
        StackEntry entry = findEntry(gameData, ctx.stackEntryId());
        if (entry == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (targetIds.size() == 1) {
            announce(gameData, entry, Map.of(targetIds.getFirst(), ctx.total()));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        playerInputService.beginCounterDistributionAssignmentChoice(gameData, ctx.controllerId(),
                new ChoiceContext.CounterDistributionAssignment(
                        ctx.sourceCard(), ctx.controllerId(), entry.getEffectsToResolve(),
                        entry.getSourcePermanentId(), null, targetIds, Map.of(), ctx.total(), 0,
                        false, null, ctx.stackEntryId()));
    }

    /** Records the finished division on the stack entry it was announced for. */
    public void announceDivision(GameData gameData, UUID stackEntryId, Map<UUID, Integer> assignments) {
        StackEntry entry = findEntry(gameData, stackEntryId);
        if (entry != null) {
            announce(gameData, entry, assignments);
        }
    }

    private void announce(GameData gameData, StackEntry entry, Map<UUID, Integer> assignments) {
        entry.announceDamageAssignments(assignments);
        String division = assignments.entrySet().stream()
                .map(assignment -> assignment.getValue() + " to " + targetName(gameData, assignment.getKey()))
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                "'s ability divides its amount: " + division + "."));
        log.info("Game {} - {} divided-damage trigger announced {}", gameData.id, entry.getCard().getName(), division);
        triggerCollectionService.checkCrimeTriggers(gameData, entry);
        triggerCollectionService.checkTargetChoiceTriggers(gameData, entry);
    }

    private static Division unannouncedDivision(StackEntry entry) {
        if (entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                || !entry.getDamageAssignments().isEmpty()) {
            return null;
        }
        for (CardEffect effect : entry.getEffectsToResolve()) {
            if (effect instanceof DealDividedDamageEffect divided
                    && divided.mode() == DivisionMode.CHOSEN
                    && divided.etbAssignments()
                    && divided.targetRestriction() == null) {
                return new Division(divided.totalDamage(), divided.maxTargets(),
                        DealDividedDamageEffect.chosenAmongAnyTargets(divided.totalDamage()), false);
            }
            if (effect instanceof DistributeCountersAmongTargetsEffect counters
                    && counters.mode() == DivisionMode.CHOSEN
                    && counters.etbAssignments()
                    && counters.targetRestriction() == null) {
                return new Division(counters.total(), 0,
                        DistributeCountersAmongTargetsEffect.chosenAmongTargetCreatures(
                                counters.counterType(), counters.total()), false);
            }
            if (effect instanceof PreventDividedDamageEffect prevention && prevention.etbAssignments()) {
                return new Division(prevention.amount(), 0,
                        new PreventDividedDamageEffect(prevention.amount()), true);
            }
        }
        return null;
    }

    /**
     * The divided part of a stacked trigger awaiting its announcement.
     *
     * @param total               the amount to divide
     * @param maxTargets          the most targets it may be divided among, {@code 0} for any number
     * @param targetingEquivalent the cast-time form of the effect, whose declared any-target spec
     *                            selects the legal targets
     * @param prevention          whether the amount is damage prevention rather than damage
     */
    private record Division(DynamicAmount total, int maxTargets, CardEffect targetingEquivalent,
                            boolean prevention) {
    }

    private Permanent sourceOf(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return source != null ? source : entry.getSourcePermanentSnapshot();
    }

    private String targetName(GameData gameData, UUID targetId) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
        return permanent != null ? permanent.getCard().getName() : gameData.playerIdToName.get(targetId);
    }

    private static StackEntry findEntry(GameData gameData, UUID stackEntryId) {
        return gameData.stack.stream()
                .filter(entry -> entry.getTargetableId().equals(stackEntryId))
                .findFirst()
                .orElse(null);
    }
}
