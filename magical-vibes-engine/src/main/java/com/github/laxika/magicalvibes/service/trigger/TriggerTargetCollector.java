package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Shared helper for collecting the list of valid UUIDs that can be chosen as the target of a
 * pending triggered ability. Centralises the logic that was previously duplicated across
 * {@code TriggeredAbilityQueueService.processNextDeathTriggerTarget},
 * {@code TriggeredAbilityQueueService.processNextAttackTriggerTarget}, and
 * {@code StepTriggerService.processNextEndStepTriggerTarget}.
 *
 * <p>By going through a single method we guarantee that trigger-slot–specific quirks (such as
 * {@link PlayerRelationPredicate} restricting targets to opponents only) are honoured consistently
 * across every slot that offers targets via the {@code pendingXxxTriggerTargets} queues.
 */
@Service
@RequiredArgsConstructor
public class TriggerTargetCollector {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetLegalityService targetLegalityService;

    /**
     * Result of a target-collection pass.
     *
     * @param validTargets      the UUIDs (player ids and/or permanent ids) the controller may pick.
     * @param canTargetPlayers  whether any effect in the trigger supports targeting a player.
     * @param canTargetPermanents whether any effect in the trigger supports targeting a permanent.
     * @param opponentOnly      {@code true} when the effect's target filter is a
     *                          {@link PlayerPredicateTargetFilter} with a
     *                          {@link PlayerRelationPredicate} of {@link PlayerRelation#OPPONENT}.
     *                          Useful for log/prompt wording.
     */
    public record Result(List<UUID> validTargets,
                         boolean canTargetPlayers,
                         boolean canTargetPermanents,
                         boolean opponentOnly) {
    }

    /**
     * Options controlling trigger-slot–specific differences. Use the predefined constants:
     * {@link #DEATH}, {@link #DELAYED_DEATH}, {@link #ATTACK}, {@link #END_STEP},
     * {@link #UPKEEP}, {@link #DAY_NIGHT}.
     *
     * @param creaturesOnly            when {@code true}, permanent candidates are restricted to
     *                                 creatures. Used by death triggers such as Black Cat.
     * @param supportControlledFilter  when {@code true}, a target filter of type
     *                                 {@link ControlledPermanentPredicateTargetFilter} is consulted
     *                                 via {@link com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService#matchesFilters}. Death, attack,
     *                                 and end-step trigger pipelines support this.
     * @param unwrapConditional        when {@code true}, {@link ConditionalEffect} wrappers are
     *                                 unwrapped before inspecting {@code canTarget*} /
     *                                 {@code targetPredicate}. End-step wraps effects in morbid /
     *                                 metalcraft / etc.
     * @param useEffectTargetPredicate when {@code true}, effects' own targeting predicate (read via
     *                                 {@code EffectResolution.targetPredicateOf}) further filters
     *                                 permanent candidates (in addition to the card-level target
     *                                 filter). Used by end-step and death triggers — a death
     *                                 trigger granted by another card (Showstopper's "creatures you
     *                                 control gain 'when this creature dies, it deals 2 damage to
     *                                 target creature an opponent controls'") has no card-level
     *                                 filter to carry its restriction, since the dying creature's
     *                                 card is not the card that granted the ability.
     */
    public record Options(boolean creaturesOnly,
                          boolean supportControlledFilter,
                          boolean unwrapConditional,
                          boolean useEffectTargetPredicate) {

        public static final Options DEATH = new Options(true, true, false, true);
        public static final Options DELAYED_DEATH = new Options(false, true, false, true);
        public static final Options ATTACK = new Options(false, true, false, true);
        public static final Options END_STEP = new Options(false, true, true, true);
        public static final Options UPKEEP = new Options(false, true, true, true);
        public static final Options DAY_NIGHT = new Options(false, true, true, true);
    }

    /**
     * Collects the UUIDs that are valid targets for the given triggered-ability context.
     *
     * @param gameData     current game state.
     * @param effects      the effects of the triggered ability.
     * @param targetFilter the card-level {@link TargetFilter} (may be {@code null}).
     * @param controllerId the controller of the trigger — used for opponent-only filtering.
     * @param sourceCard   the source card (used for {@link FilterContext}).
     * @param options      trigger-slot–specific behaviour toggles.
     * @return the collected {@link Result}; {@link Result#validTargets} may be empty.
     */
    public Result collect(GameData gameData,
                          List<CardEffect> effects,
                          TargetFilter targetFilter,
                          UUID controllerId,
                          Card sourceCard,
                          Options options) {
        return collect(gameData, effects, targetFilter, controllerId, sourceCard, options, null);
    }

    /**
     * Collects targets while optionally supplying a permanent that an enter-trigger effect uses
     * as its comparison source. Ordinary trigger targeting leaves this snapshot null.
     */
    public Result collect(GameData gameData,
                          List<CardEffect> effects,
                          TargetFilter targetFilter,
                          UUID controllerId,
                          Card sourceCard,
                          Options options,
                          Permanent sourcePermanentSnapshot) {
        return collect(gameData, effects, targetFilter, controllerId, sourceCard, options,
                sourcePermanentSnapshot, null);
    }

    /**
     * Collects targets with the player that was attacked by the trigger's source, when the
     * trigger captured a combat-damage event. This lets defending-player filters use the event's
     * last known combat target after combat state has been cleared.
     */
    public Result collect(GameData gameData,
                          List<CardEffect> effects,
                          TargetFilter targetFilter,
                          UUID controllerId,
                          Card sourceCard,
                          Options options,
                          Permanent sourcePermanentSnapshot,
                          UUID defendingPlayerId) {

        boolean canTargetPlayers = effects.stream()
                .map(e -> unwrap(e, options))
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        boolean canTargetPermanents = effects.stream()
                .map(e -> unwrap(e, options))
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));

        // An effect narrows the player half on its own only when it says so through
        // CardEffect.targetPlayerRelation() (Scalding Tongs' "target opponent or planeswalker").
        // The declared target cannot express it: playerOrPlaneswalker() is shared with "target
        // player or planeswalker" (Goblin Razerunners), where the controller is a legal choice.
        boolean effectsAreOpponentOnly = !effects.isEmpty() && effects.stream()
                .map(e -> unwrap(e, options))
                .allMatch(e -> e.targetPlayerRelation() == PlayerRelation.OPPONENT);

        boolean opponentOnly = isOpponentRestricted(targetFilter) || effectsAreOpponentOnly;

        List<UUID> validTargets = new ArrayList<>();

        if (canTargetPlayers) {
            if (opponentOnly) {
                for (UUID pid : gameData.orderedPlayerIds) {
                    if (!pid.equals(controllerId)) {
                        validTargets.add(pid);
                    }
                }
            } else {
                validTargets.addAll(gameData.orderedPlayerIds);
            }
        }

        if (canTargetPermanents) {
            FilterContext filterCtx = targetFilter != null
                    ? new FilterContext(gameData, sourceCard.getId(), controllerId, null, sourcePermanentSnapshot)
                    .withDefendingPlayerId(defendingPlayerId)
                    : null;

            PermanentPredicate effectPredicate = null;
            FilterContext effectFilterCtx = null;
            if (options.useEffectTargetPredicate()) {
                List<CardEffect> targetingEffects = effects.stream()
                        .map(e -> unwrap(e, options))
                        .filter(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                        .toList();
                effectPredicate = EffectResolution.declaredPermanentRestriction(targetingEffects)
                        .orElse(null);
                if (effectPredicate != null) {
                    effectFilterCtx = new FilterContext(gameData, sourceCard.getId(), controllerId, null,
                            sourcePermanentSnapshot).withDefendingPlayerId(defendingPlayerId);
                }
            }

            // An explicit PermanentPredicateTargetFilter fully governs which permanents are legal
            // targets, so the death pipeline's default "creatures only" narrowing must not intersect
            // it away (e.g. Fire Snake's "destroy target land"). The permanent side of an
            // AnyTargetPredicateTargetFilter governs in the same way (Scuttling Doom Engine's death
            // trigger reaches planeswalkers, not creatures).
            boolean explicitPermanentFilter = targetFilter instanceof PermanentPredicateTargetFilter
                    || targetFilter instanceof AnyTargetPredicateTargetFilter;
            boolean creaturesOnly = options.creaturesOnly() && !explicitPermanentFilter;

            // Effects that declare a cross-kind target — CR 115.4's anyTarget() (Flameblast Dragon
            // attack trigger, Form of the Dragon upkeep) or playerOrPlaneswalker() (Scalding Tongs)
            // — restrict the permanent half to the permanent kinds that target admits, never a land
            // or other kind. The restriction is *evaluated* from the declared target rather than
            // re-implemented here, so this path cannot drift from the spell path in
            // ValidTargetService / TargetValidationService, and it is layer-aware (CR 613.1d): a
            // planeswalker a type-replacing effect turned into a land is no longer an any target.
            // An explicit PermanentPredicateTargetFilter fully governs instead (e.g. destroy land).
            List<CardEffect> permanentEffects = effects.stream()
                    .map(e -> unwrap(e, options))
                    .filter(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                    .toList();
            TargetPredicate declaredPermanentTarget = null;
            if (!explicitPermanentFilter && !permanentEffects.isEmpty()) {
                for (TargetPredicate candidate : List.of(TargetPredicates.anyTarget(),
                        TargetPredicates.playerOrPlaneswalker())) {
                    if (permanentEffects.stream().allMatch(e -> e.targetSpec().declares(candidate))) {
                        declaredPermanentTarget = candidate;
                        break;
                    }
                }
            }
            if (declaredPermanentTarget != null) {
                creaturesOnly = false;
            }
            PermanentPredicate declaredTargetRestriction = declaredPermanentTarget != null
                    ? declaredPermanentTarget.permanentRestriction().orElseThrow()
                    : null;
            FilterContext declaredTargetFilterCtx = declaredTargetRestriction != null
                    ? new FilterContext(gameData, sourceCard.getId(), controllerId, null, null)
                    : null;

            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (creaturesOnly && !gameQueryService.isCreature(gameData, p)) continue;
                    if (gameQueryService.cantBeTargetedByWallOnlySources(gameData, p)
                            && targetLegalityService.sourceCanTargetOnlyWalls(sourceCard, effects, targetFilter)) {
                        continue;
                    }

                    if (declaredTargetRestriction != null && !predicateEvaluationService.matchesPermanentPredicate(
                            p, declaredTargetRestriction, declaredTargetFilterCtx)) {
                        continue;
                    }

                    if (options.supportControlledFilter()
                            && targetFilter instanceof ControlledPermanentPredicateTargetFilter cpf) {
                        if (!predicateEvaluationService.matchesFilters(p, Set.of(cpf), filterCtx)) continue;
                    } else if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
                        if (!predicateEvaluationService.matchesPermanentPredicate(p, ppf.predicate(), filterCtx)) continue;
                    } else if (targetFilter instanceof AnyTargetPredicateTargetFilter anyFilter) {
                        if (!predicateEvaluationService.matchesPermanentPredicate(
                                p, anyFilter.permanentPredicate(), filterCtx)) continue;
                    }

                    if (effectPredicate != null
                            && !predicateEvaluationService.matchesPermanentPredicate(p, effectPredicate, effectFilterCtx)) {
                        continue;
                    }

                    validTargets.add(p.getId());
                }
            }
        }

        return new Result(validTargets, canTargetPlayers, canTargetPermanents, opponentOnly);
    }

    /**
     * Whether the card-level filter restricts player targets to opponents. Both a plain
     * {@link PlayerPredicateTargetFilter} ("target opponent") and the player side of an
     * {@link AnyTargetPredicateTargetFilter} ("target opponent or planeswalker") express this.
     */
    private static boolean isOpponentRestricted(TargetFilter targetFilter) {
        PlayerPredicate playerPredicate = switch (targetFilter) {
            case PlayerPredicateTargetFilter ppf -> ppf.predicate();
            case AnyTargetPredicateTargetFilter anyFilter -> anyFilter.playerPredicate();
            case null, default -> null;
        };
        return playerPredicate instanceof PlayerRelationPredicate prp
                && prp.relation() == PlayerRelation.OPPONENT;
    }

    /**
     * Looks through the wrappers that hide an effect's own targeting from this collector.
     *
     * <p>{@link MayEffect} and {@link MayPayManaEffect} carry the target restriction on the
     * effective wrapped or else effect, while the target of the ability as a whole is still chosen
     * when the trigger is put on the stack. {@link ConditionalEffect} is unwrapped only for the slots whose
     * {@link Options#unwrapConditional()} says so.
     */
    private static CardEffect unwrap(CardEffect effect, Options options) {
        CardEffect unwrapped = switch (effect) {
            case MayEffect may -> effectiveTargetEffect(may.wrapped(), may.elseEffect(), effect);
            case MayPayManaEffect mayPay -> effectiveTargetEffect(
                    mayPay.wrapped(), mayPay.elseEffect(), effect);
            default -> effect;
        };
        return options.unwrapConditional() && unwrapped instanceof ConditionalEffect ce ? ce.wrapped() : unwrapped;
    }

    private static CardEffect effectiveTargetEffect(CardEffect wrapped, CardEffect elseEffect,
                                                    CardEffect fallback) {
        if (hasTargetingSpec(wrapped)) {
            return wrapped;
        }
        if (hasTargetingSpec(elseEffect)) {
            return elseEffect;
        }
        return fallback;
    }

    private static boolean hasTargetingSpec(CardEffect effect) {
        return effect != null && !TargetSpec.NONE.equals(effect.targetSpec());
    }
}
