package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
     * {@link #DEATH}, {@link #ATTACK}, {@link #END_STEP}.
     *
     * @param creaturesOnly            when {@code true}, permanent candidates are restricted to
     *                                 creatures. Used by death triggers such as Black Cat.
     * @param supportControlledFilter  when {@code true}, a target filter of type
     *                                 {@link ControlledPermanentPredicateTargetFilter} is consulted
     *                                 via {@link GameQueryService#matchesFilters}. Death and attack
     *                                 trigger pipelines support this; end-step does not.
     * @param unwrapConditional        when {@code true}, {@link ConditionalEffect} wrappers are
     *                                 unwrapped before inspecting {@code canTarget*} /
     *                                 {@code targetPredicate}. End-step wraps effects in morbid /
     *                                 metalcraft / etc.
     * @param useEffectTargetPredicate when {@code true}, effects' own
     *                                 {@link CardEffect#targetPredicate()} values further filter
     *                                 permanent candidates (in addition to the card-level target
     *                                 filter). Used by end-step triggers.
     */
    public record Options(boolean creaturesOnly,
                          boolean supportControlledFilter,
                          boolean unwrapConditional,
                          boolean useEffectTargetPredicate) {

        public static final Options DEATH = new Options(true, true, false, false);
        public static final Options ATTACK = new Options(false, true, false, false);
        public static final Options END_STEP = new Options(false, false, true, true);
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

        boolean canTargetPlayers = effects.stream()
                .map(e -> options.unwrapConditional() && e instanceof ConditionalEffect ce ? ce.wrapped() : e)
                .anyMatch(CardEffect::canTargetPlayer);
        boolean canTargetPermanents = effects.stream()
                .map(e -> options.unwrapConditional() && e instanceof ConditionalEffect ce ? ce.wrapped() : e)
                .anyMatch(CardEffect::canTargetPermanent);

        boolean opponentOnly = targetFilter instanceof PlayerPredicateTargetFilter ppf
                && ppf.predicate() instanceof PlayerRelationPredicate prp
                && prp.relation() == PlayerRelation.OPPONENT;

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
                    ? new FilterContext(gameData, sourceCard.getId(), controllerId, null)
                    : null;

            PermanentPredicate effectPredicate = null;
            FilterContext effectFilterCtx = null;
            if (options.useEffectTargetPredicate()) {
                effectPredicate = effects.stream()
                        .map(e -> e instanceof ConditionalEffect ce ? ce.wrapped() : e)
                        .filter(e -> e.canTargetPermanent() && e.targetPredicate() != null)
                        .map(CardEffect::targetPredicate)
                        .findFirst().orElse(null);
                if (effectPredicate != null) {
                    effectFilterCtx = new FilterContext(gameData, sourceCard.getId(), controllerId, null);
                }
            }

            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) continue;
                for (Permanent p : battlefield) {
                    if (options.creaturesOnly() && !gameQueryService.isCreature(gameData, p)) continue;

                    if (options.supportControlledFilter()
                            && targetFilter instanceof ControlledPermanentPredicateTargetFilter cpf) {
                        if (!gameQueryService.matchesFilters(p, Set.of(cpf), filterCtx)) continue;
                    } else if (targetFilter instanceof PermanentPredicateTargetFilter ppf) {
                        if (!gameQueryService.matchesPermanentPredicate(p, ppf.predicate(), filterCtx)) continue;
                    }

                    if (effectPredicate != null
                            && !gameQueryService.matchesPermanentPredicate(p, effectPredicate, effectFilterCtx)) {
                        continue;
                    }

                    validTargets.add(p.getId());
                }
            }
        }

        return new Result(validTargets, canTargetPlayers, canTargetPermanents, opponentOnly);
    }
}
