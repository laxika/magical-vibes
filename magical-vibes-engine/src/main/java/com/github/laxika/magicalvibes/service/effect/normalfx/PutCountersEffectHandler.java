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
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
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
 *   <li>{@code source()} → the ability's own source permanent, or its face-up planar object for a planar ability;
 *       the entry's source snapshot is refreshed after the placement;</li>
 *   <li>another non-target subject → the permanents {@code SubjectResolutionService} names (non-targeting); a set
 *       ("each creature you control") gets one amount, worked out against the source (CR 608.2h), unless the effect
 *       works it out for each permanent;</li>
 *   <li>a non-null {@code predicate} → resolution-time choice among the controller's matching
 *       permanents (non-targeting);</li>
 *   <li>otherwise a targeting effect, resolving {@code StackEntry.targetsForEffect} — the
 *       effect's target-group slice for multi-group spells (e.g. River Heralds' Boon), the
 *       full {@code targetIds} list for an unbound effect that targets several permanents, or
 *       the lone {@code targetId}.</li>
 * </ul>
 * The counter count is a {@code DynamicAmount} ({@code Fixed}, {@code XValue()}, …). Placement is
 * routed through {@link PermanentCounterSupport#placeCounterInBatch} so counter-type-specific
 * behaviour (+1/+1 triggers, -1/-1 prevention, lore/saga chapters) is preserved; every placement of one resolution is
 * one batch, whose "one or more" events (Heroes, loyalty on planeswalkers) are raised once after the last placement,
 * also when a replacement order made it wait (CR 603.2c). When
 * {@code regenerateIfSurvives} is set, the target is regenerated after placement if it survives.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCountersEffectHandler implements NormalEffectHandlerBean {

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
        return PutCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersEffect) effect;
        Map<UUID, Integer> before = permanentCounterSupport.counterCounts(gameData,
                candidates(gameData, entry, e), e.counterType());
        List<ChoiceContext.CounterPlacement> batch = new ArrayList<>();
        boolean waiting = putCounters(gameData, entry, e, batch);
        entry.recordStep(StepKind.PUT_COUNTERS,
                permanentCounterSupport.counterChange(gameData, before, e.counterType(), true));
        if (!waiting) {
            permanentCounterSupport.firePlacementBatchEvents(gameData, e.counterType(), batch);
        }
    }

    /**
     * Every permanent the effect may put counters on: its subject's, its targets, or (for a predicate choice) every
     * permanent its controller controls; the step's result is read from their counts.
     */
    private List<UUID> candidates(GameData gameData, StackEntry entry, PutCountersEffect e) {
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

    /**
     * Puts the counters, each placement joining {@code batch}; {@code true} when a placement waits for its controller
     * to order counter replacements (the rest of the batch then goes on after the answer).
     */
    private boolean putCounters(GameData gameData, StackEntry entry, PutCountersEffect e,
                                List<ChoiceContext.CounterPlacement> batch) {
        CardEffect effect = e;
        if (e.subject() instanceof PermanentSubject.Source) {
            return resolveSource(gameData, entry, e, batch);
        }
        if (!(e.subject() instanceof PermanentSubject.Target)) {
            return resolveNamedPermanents(gameData, entry, e, batch);
        }
        int count = amountAgainstSource(gameData, entry, e);

        // Predicate-based resolution: choose from controller's battlefield (non-targeting).
        if (e.predicate() != null) {
            permanentCounterSupport.resolveCounterOnOwnPermanent(gameData, entry,
                    e.counterType(), count, e.predicate());
            return false;
        }

        // Targeting mode: apply to each valid target of this effect's target group — the group's
        // slice of the flat target list for effects bound via target(...).addEffect(...) (e.g.
        // River Heralds' Boon, Homesickness), the whole flat list for unbound effects, or the
        // lone targetId for single-target entries. An empty group (optional target not chosen)
        // does nothing.
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.targetsForBoundEffectGroup(effect) != null) {
            return false;
        }
        if (!targetIds.isEmpty()) {
            return resolveTargets(gameData, entry, targetIds, e, count, batch);
        }

        // Single-target fallback.
        if (entry.getTargetId() == null) {
            log.info("Game {} - Target no longer on battlefield, effect fizzles", gameData.id);
            return false;
        }
        return resolveTargets(gameData, entry, List.of(entry.getTargetId()), e, count, batch);
    }

    /** The effect's amount worked out against its source as it is, else as it last existed (CR 608.2h). */
    private int amountAgainstSource(GameData gameData, StackEntry entry, PutCountersEffect e) {
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        return amountEvaluationService.evaluate(gameData, e.amount(), AmountContext.forStackEntry(entry, source));
    }

    /** Whether the effect's one amount, worked out against its source, goes on each permanent of its set. */
    private static boolean oneAmountForSet(PutCountersEffect e) {
        return e.subject().isSet() && !e.amountForEachRecipient();
    }

    /**
     * A subject that names its permanents without targeting them ("put a +1/+1 counter on it", "on enchanted
     * creature"): each named permanent still on the battlefield gets the counters, the count evaluated against that
     * permanent ("equal to its power"), wherever it is now, even if control changed before resolution. A set ("each
     * creature you control") is found once and gets one amount, worked out against the source (CR 608.2h), unless the
     * effect works it out for each permanent.
     */
    private boolean resolveNamedPermanents(GameData gameData, StackEntry entry, PutCountersEffect e,
                                           List<ChoiceContext.CounterPlacement> batch) {
        List<Permanent> named = subjectResolutionService.permanents(gameData, entry, e, e.subject());
        if (oneAmountForSet(e)) {
            return resolveTargets(gameData, entry, named.stream().map(Permanent::getId).toList(), e,
                    amountAgainstSource(gameData, entry, e), batch);
        }
        return placeOnNamed(gameData, entry, e, named, batch);
    }

    /**
     * "Put N counters on this [permanent]": the source permanent, else a planar ability's face-up planar object. After
     * the placement the entry's source snapshot shows the new counters, unless the permanent has transformed since
     * the ability was put on the stack (later steps can then still tell it transformed).
     */
    private boolean resolveSource(GameData gameData, StackEntry entry, PutCountersEffect e,
                                  List<ChoiceContext.CounterPlacement> batch) {
        if (entry.getSourcePlanarObject() != null && entry.getSourcePermanentId() == null) {
            resolvePlanarSource(gameData, entry, e);
            return false;
        }
        List<Permanent> self = subjectResolutionService.permanents(gameData, entry, e, e.subject());
        if (placeOnNamed(gameData, entry, e, self, batch)) {
            return true;
        }
        if (self.isEmpty()) {
            return false;
        }
        Permanent source = self.getFirst();
        Permanent previousSnapshot = entry.getSourcePermanentSnapshot();
        if (previousSnapshot == null || !previousSnapshot.getId().equals(source.getId())
                || previousSnapshot.getTransformationSequence() == source.getTransformationSequence()) {
            entry.setSourcePermanentSnapshot(new Permanent(source));
        }
        return false;
    }

    private void resolvePlanarSource(GameData gameData, StackEntry entry, PutCountersEffect e) {
        PlanarObject source = gameData.planechase == null ? null
                : gameData.planechase.faceUp.stream()
                .filter(object -> object.getId().equals(entry.getSourcePlanarObject().getId()))
                .findFirst()
                .orElse(null);
        if (source == null) {
            return;
        }
        int count = amountEvaluationService.evaluate(gameData, e.amount(), AmountContext.forStackEntry(entry, null));
        if (count <= 0) {
            return;
        }
        source.getCounters().merge(e.counterType(), count, Integer::sum);
        entry.setSourcePlanarObject(source.copy());
    }

    /**
     * Places on each of {@code named} still on the battlefield, the count evaluated against that permanent. Returns
     * {@code true} when a placement waits for its controller to order counter replacements.
     */
    private boolean placeOnNamed(GameData gameData, StackEntry entry, PutCountersEffect e,
                                 List<Permanent> named, List<ChoiceContext.CounterPlacement> batch) {
        for (int i = 0; i < named.size(); i++) {
            Permanent permanent = named.get(i);
            int count = amountEvaluationService.evaluate(gameData, e.amount(),
                    AmountContext.forStackEntry(entry, permanent));
            List<UUID> later = named.subList(i + 1, named.size()).stream().map(Permanent::getId).toList();
            if (placeOnTarget(gameData, entry, permanent.getId(), e, count, later, false, batch)) {
                return true;
            }
        }
        return false;
    }

    /** Places {@code count} on each of {@code targets}; {@code true} when a placement waits for a replacement order. */
    private boolean resolveTargets(GameData gameData, StackEntry entry, List<UUID> targets,
                                   PutCountersEffect effect, int count, List<ChoiceContext.CounterPlacement> batch) {
        for (int i = 0; i < targets.size(); i++) {
            if (placeOnTarget(gameData, entry, targets.get(i), effect, count,
                    targets.subList(i + 1, targets.size()), false, batch)) {
                return true;
            }
        }
        return false;
    }

    private boolean placeOnTarget(GameData gameData, StackEntry entry, UUID targetId,
                                   PutCountersEffect e, int count,
                                   List<UUID> subsequentTargets, boolean replacementsApplied,
                                   List<ChoiceContext.CounterPlacement> batch) {
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
        UUID placerId = placerOf(gameData, entry, e, target);

        if (!counterBlocked && !replacementsApplied && count > 0) {
            var modifiers = gameQueryService.counterReplacementsFor(gameData, target, e.counterType(),
                    placerId, false);
            if (gameQueryService.counterReplacementOrderMatters(gameData, e.counterType(), count, modifiers)) {
                beginReplacementChoice(gameData, new ChoiceContext.CounterReplacementOrder(
                        entry, targetId, count, modifiers, e, subsequentTargets, count, batch));
                return true;
            }
        }
        // A prevented placement does not stop later instructions (CR 608.2c), so a "then"
        // regeneration clause is still checked against the target's toughness.
        if (!counterBlocked) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
            boolean planeswalker = gameQueryService.isPlaneswalker(gameData, target);
            int placed = permanentCounterSupport.placeCounterInBatch(gameData, entry, target, e.counterType(), count,
                    placerId, replacementsApplied);
            if (placed > 0) {
                batch.add(new ChoiceContext.CounterPlacement(target.getId(), placerId, controllerId, planeswalker,
                        placed));
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
     * The player who puts the counters on {@code target}: the effect's controller, or the permanent's controller when
     * each controller puts them on their own permanents.
     */
    private UUID placerOf(GameData gameData, StackEntry entry, PutCountersEffect e,
                          Permanent target) {
        if (e.eachControllerPlaces()) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
            if (controllerId != null) {
                return controllerId;
            }
        }
        return entry.getControllerId();
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
                    order.originalCount(), order.placed()));
            return;
        }
        for (var replacement : remaining) {
            count = gameQueryService.applyCounterReplacement(gameData, type, count, replacement);
        }
        List<UUID> rest = new ArrayList<>(List.of(order.targetId()));
        rest.addAll(order.subsequentTargets());
        Map<UUID, Integer> before = permanentCounterSupport.counterCounts(gameData, rest, type);
        List<ChoiceContext.CounterPlacement> batch = new ArrayList<>(order.placed());
        placeOnTarget(gameData, order.entry(), order.targetId(), order.effect(), count, List.of(), true, batch);
        boolean waiting;
        if (order.effect().subject() instanceof PermanentSubject.Target || oneAmountForSet(order.effect())) {
            // A target's or a set's count was worked out once, before the placement waited.
            waiting = resolveTargets(gameData, order.entry(), order.subsequentTargets(), order.effect(),
                    order.originalCount(), batch);
        } else {
            // A named permanent's count is its own ("equal to its power"), as if the placement had not waited.
            waiting = placeOnNamed(gameData, order.entry(), order.effect(), order.subsequentTargets().stream()
                    .map(id -> gameQueryService.findPermanentById(gameData, id))
                    .filter(java.util.Objects::nonNull)
                    .toList(), batch);
        }
        // The placement waited for this order, so its step recorded only what came before; the rest is added now.
        order.entry().addToStep(StepKind.PUT_COUNTERS, permanentCounterSupport.counterChange(gameData, before, type,
                true));
        if (!waiting) {
            permanentCounterSupport.firePlacementBatchEvents(gameData, type, batch);
        }
    }
}
