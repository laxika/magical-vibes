package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.RegenerationShield;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import java.util.ArrayList;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Puts counters on a permanent. Handles the full counter-placement family:
 * <ul>
 *   <li>a non-target subject → the permanents {@code SubjectResolutionService} names (non-targeting);</li>
 *   <li>a non-null {@code predicate} → resolution-time choice among the controller's matching
 *       permanents (non-targeting);</li>
 *   <li>otherwise a targeting effect, resolving {@code StackEntry.targetsForEffect} — the
 *       effect's target-group slice for multi-group spells (e.g. River Heralds' Boon), the
 *       full {@code targetIds} list for an unbound effect that targets several permanents, or
 *       the lone {@code targetId}.</li>
 * </ul>
 * The counter count is a {@code DynamicAmount} ({@code Fixed}, {@code XValue()}, …). Placement is
 * routed through {@link PermanentCounterSupport#placeCounterOnPermanent} so counter-type-specific
 * behaviour (+1/+1 triggers, -1/-1 prevention, lore/saga chapters) is preserved. When
 * {@code regenerateIfSurvives} is set, the target is regenerated after placement if it survives.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCounterOnTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Autowired @Lazy
    private InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCounterOnTargetPermanentEffect) effect;
        Map<UUID, Integer> before = permanentCounterSupport.counterCounts(gameData,
                candidates(gameData, entry, e), e.counterType());
        putCounters(gameData, entry, e);
        entry.recordStep(StepKind.PUT_COUNTERS,
                permanentCounterSupport.counterChange(gameData, before, e.counterType(), true));
    }

    /**
     * Every permanent the effect may put counters on: its subject's, its targets, or (for a predicate choice) every
     * permanent its controller controls; the step's result is read from their counts.
     */
    private List<UUID> candidates(GameData gameData, StackEntry entry, PutCounterOnTargetPermanentEffect e) {
        if (!(e.subject() instanceof PermanentSubject.Target)) {
            return subjectResolutionService.permanents(gameData, entry, e, e.subject()).stream()
                    .map(Permanent::getId).toList();
        }
        if (e.predicate() != null) {
            return gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of()).stream()
                    .map(Permanent::getId).toList();
        }
        List<UUID> targetIds = entry.targetsForEffect(e);
        if (!targetIds.isEmpty()) {
            return targetIds;
        }
        return entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
    }

    private void putCounters(GameData gameData, StackEntry entry, PutCounterOnTargetPermanentEffect e) {
        CardEffect effect = e;
        if (!(e.subject() instanceof PermanentSubject.Target)) {
            resolveNamedPermanents(gameData, entry, e);
            return;
        }
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int count = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));

        // Predicate-based resolution: choose from controller's battlefield (non-targeting).
        if (e.predicate() != null) {
            permanentCounterSupport.resolveCounterOnOwnPermanent(gameData, entry,
                    e.counterType(), count, e.predicate());
            return;
        }

        // Targeting mode: apply to each valid target of this effect's target group — the group's
        // slice of the flat target list for effects bound via target(...).addEffect(...) (e.g.
        // River Heralds' Boon, Homesickness), the whole flat list for unbound effects, or the
        // lone targetId for single-target entries. An empty group (optional target not chosen)
        // does nothing.
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.targetsForBoundEffectGroup(effect) != null) {
            return;
        }
        if (!targetIds.isEmpty()) {
            resolveTargets(gameData, entry, targetIds, e, count);
            return;
        }

        // Single-target fallback.
        if (entry.getTargetId() == null) {
            log.info("Game {} - Target no longer on battlefield, effect fizzles", gameData.id);
            return;
        }
        resolveTargets(gameData, entry, List.of(entry.getTargetId()), e, count);
    }

    /**
     * A subject that names its permanents without targeting them ("put a +1/+1 counter on it", "on enchanted
     * creature"): each named permanent still on the battlefield gets the counters, the count evaluated against that
     * permanent ("equal to its power"), wherever it is now, even if control changed before resolution.
     */
    private void resolveNamedPermanents(GameData gameData, StackEntry entry, PutCounterOnTargetPermanentEffect e) {
        List<Permanent> named = subjectResolutionService.permanents(gameData, entry, e, e.subject());
        for (int i = 0; i < named.size(); i++) {
            Permanent permanent = named.get(i);
            int count = amountEvaluationService.evaluate(gameData, e.amount(),
                    AmountContext.forStackEntry(entry, permanent));
            List<UUID> later = named.subList(i + 1, named.size()).stream().map(Permanent::getId).toList();
            if (placeOnTarget(gameData, entry, permanent.getId(), e, count, later, false)) {
                return;
            }
        }
    }

    private void resolveTargets(GameData gameData, StackEntry entry, List<UUID> targets,
                                PutCounterOnTargetPermanentEffect effect, int count) {
        for (int i = 0; i < targets.size(); i++) {
            if (placeOnTarget(gameData, entry, targets.get(i), effect, count,
                    targets.subList(i + 1, targets.size()), false)) {
                return;
            }
        }
    }

    private boolean placeOnTarget(GameData gameData, StackEntry entry, UUID targetId,
                                   PutCounterOnTargetPermanentEffect e, int count,
                                   List<UUID> subsequentTargets, boolean replacementsApplied) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return false; // Partially resolves — skip removed targets.
        }
        // Resolution-time gate ("if it's legendary" — Ancient Animus): target stays legal,
        // the counters just aren't placed when the condition doesn't hold.
        if (e.resolutionCondition() != null
                && !predicateEvaluationService.matchesPermanentPredicate(target, e.resolutionCondition(),
                        FilterContext.of(gameData)
                                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                                .withSourceControllerId(entry.getControllerId())
                                .withSourcePermanentId(entry.getSourcePermanentId())
                                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot()))) {
            return false;
        }
        boolean counterBlocked = gameQueryService.cantHaveCounters(gameData, target)
                || (e.counterType() == CounterType.MINUS_ONE_MINUS_ONE
                && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target));

        if (!counterBlocked && !replacementsApplied && count > 0) {
            var modifiers = gameQueryService.counterReplacementsFor(gameData, target, e.counterType(),
                    entry.getControllerId(), false);
            if (gameQueryService.counterReplacementOrderMatters(gameData, e.counterType(), count, modifiers)) {
                beginReplacementChoice(gameData, new ChoiceContext.CounterReplacementOrder(
                        entry, targetId, count, modifiers, e, subsequentTargets, count));
                return true;
            }
        }
        // A prevented placement does not stop later instructions (CR 608.2c), so a "then"
        // regeneration clause is still checked against the target's toughness.
        if (!counterBlocked) {
            if (replacementsApplied) {
                permanentCounterSupport.placeCounterOnPermanentAfterReplacements(
                        gameData, entry, target, e.counterType(), count);
            } else {
                permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target, e.counterType(), count);
            }
        }

        if (e.regenerateIfSurvives()) {
            int effectiveToughness = gameQueryService.getEffectiveToughness(gameData, target);
            if (effectiveToughness >= 1) {
                gameData.addEffectOnPermanent(entry, RegenerationShield.PLAIN, target.getId(), Duration.UNTIL_END_OF_TURN);
                gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " gains a regeneration shield."));
                log.info("Game {} - {} gains a regeneration shield (toughness {})",
                        gameData.id, target.getCard().getName(), effectiveToughness);
            }
        }
        return false;
    }

    /**
     * For counter placements that are not themselves {@code PutCounterOnTargetPermanentEffect}
     * (e.g. "put a +1/+1 counter on this creature"): when two or more counter replacement effects
     * apply and the order changes the result, asks the permanent's controller to order them
     * (CR 616.1) and resumes the placement from the answer. Returns {@code true} when that choice
     * was started (the caller must stop; the placement completes when it is answered).
     */
    public boolean beginReplacementOrderIfNeeded(GameData gameData, StackEntry entry, Permanent target,
                                                 CounterType counterType, int count) {
        if (count <= 0 || gameQueryService.cantHaveCounters(gameData, target)) {
            return false;
        }
        var modifiers = gameQueryService.counterReplacementsFor(gameData, target, counterType,
                entry.getControllerId(), false);
        if (!gameQueryService.counterReplacementOrderMatters(gameData, counterType, count, modifiers)) {
            return false;
        }
        beginReplacementChoice(gameData, new ChoiceContext.CounterReplacementOrder(
                entry, target.getId(), count, modifiers,
                new PutCounterOnTargetPermanentEffect(counterType, count), List.of(), count));
        return true;
    }

    private void beginReplacementChoice(GameData gameData, ChoiceContext.CounterReplacementOrder order) {
        UUID controller = gameQueryService.findPermanentController(gameData, order.targetId());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controller, null, null, order,
                order.remaining().stream().map(ChoiceContext.CounterReplacement::label).toList(),
                "Choose the counter replacement effect to apply next."));
    }

    /** Applies the chosen replacement once and resumes the original placement and later targets. */
    public void resolveReplacementOrder(GameData gameData, ChoiceContext.CounterReplacementOrder order,
                                         String label) {
        var remaining = new ArrayList<>(order.remaining());
        var selected = remaining.stream().filter(replacement -> replacement.label().equals(label)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid counter replacement effect"));
        remaining.remove(selected);
        CounterType type = order.effect().counterType();
        int count = gameQueryService.applyCounterReplacement(gameData, type, order.count(), selected);
        if (count > 0 && gameQueryService.counterReplacementOrderMatters(gameData, type, count, remaining)) {
            beginReplacementChoice(gameData, new ChoiceContext.CounterReplacementOrder(order.entry(),
                    order.targetId(), count, remaining, order.effect(), order.subsequentTargets(),
                    order.originalCount()));
            return;
        }
        for (var replacement : remaining) {
            count = gameQueryService.applyCounterReplacement(gameData, type, count, replacement);
        }
        List<UUID> rest = new ArrayList<>(List.of(order.targetId()));
        rest.addAll(order.subsequentTargets());
        Map<UUID, Integer> before = permanentCounterSupport.counterCounts(gameData, rest, type);
        placeOnTarget(gameData, order.entry(), order.targetId(), order.effect(), count, List.of(), true);
        resolveTargets(gameData, order.entry(), order.subsequentTargets(), order.effect(), order.originalCount());
        // The placement waited for this order, so its step recorded only what came before; the rest is added now.
        order.entry().addToStep(StepKind.PUT_COUNTERS, permanentCounterSupport.counterChange(gameData, before, type,
                true));
    }
}
