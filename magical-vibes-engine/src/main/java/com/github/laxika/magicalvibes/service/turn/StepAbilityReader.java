package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Reads the "at the beginning of [step or phase]" abilities (CR 603.2b) for the step and phase readers
 * ({@link StepTriggerService}, the planar die's step triggers) and judges their options as they trigger, so no reader
 * of them takes the legacy view's wrappers. An ability triggers only when its event condition (CR 603.2) and its
 * intervening "if" (CR 603.4) are true in the reader's {@link ConditionContext} (its source, its controller and, for
 * "that player", the player whose step it is), and while "this ability triggers only once" hasn't been used up; the
 * intervening "if" is wrapped around each of its effects as a {@link ConditionalEffect}, which checks it again as the
 * ability resolves. The effects then go to the reader's handling of their kinds, until step 15 of
 * effect-slot-refactor.md makes them read the event. The event condition isn't checked again.
 */
public final class StepAbilityReader {

    private final ConditionEvaluationService conditionEvaluationService;

    public StepAbilityReader(ConditionEvaluationService conditionEvaluationService) {
        this.conditionEvaluationService = Objects.requireNonNull(conditionEvaluationService,
                "conditionEvaluationService");
    }

    /**
     * The abilities granted to {@code permanent} for a duration with {@code trigger}: those until end of turn, end of
     * combat or next upkeep, then those until it leaves the battlefield, in that order.
     */
    public static List<TriggeredAbility> granted(Permanent permanent, StepTrigger trigger) {
        List<TriggeredAbility> abilities = new ArrayList<>(temporary(permanent, trigger));
        abilities.addAll(persistent(permanent, trigger));
        return abilities;
    }

    /** The abilities with {@code trigger} granted to {@code permanent} until end of turn, end of combat or next upkeep. */
    public static List<TriggeredAbility> temporary(Permanent permanent, StepTrigger trigger) {
        return withTrigger(permanent.getTemporaryTriggeredAbilities(), trigger);
    }

    /** The abilities with {@code trigger} granted to {@code permanent} until it leaves the battlefield. */
    public static List<TriggeredAbility> persistent(Permanent permanent, StepTrigger trigger) {
        return withTrigger(permanent.getPersistentTriggeredAbilities(), trigger);
    }

    /** The abilities of {@code abilities} that have {@code trigger} as a trigger condition, in order. */
    public static List<TriggeredAbility> withTrigger(List<TriggeredAbility> abilities, StepTrigger trigger) {
        List<TriggeredAbility> withTrigger = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            if (ability.hasTrigger(trigger)) {
                withTrigger.add(ability);
            }
        }
        return withTrigger;
    }

    /**
     * The effects of the abilities of {@code abilities} that trigger now, in order, judged in {@code context} as
     * {@link #triggeredEffects(GameData, TriggeredAbility, ConditionContext)} says.
     */
    public List<CardEffect> effects(GameData gameData, List<TriggeredAbility> abilities, ConditionContext context) {
        List<CardEffect> effects = new ArrayList<>();
        for (List<CardEffect> abilityEffects : effectsByAbility(gameData, abilities, context)) {
            effects.addAll(abilityEffects);
        }
        return effects;
    }

    /**
     * The effects of each ability of {@code abilities} that triggers now, one list for each ability (one stack object,
     * CR 603.3), in order, judged in {@code context} as {@link #triggeredEffects(GameData, TriggeredAbility,
     * ConditionContext)} says.
     */
    public List<List<CardEffect>> effectsByAbility(GameData gameData, List<TriggeredAbility> abilities,
                                                   ConditionContext context) {
        List<List<CardEffect>> byAbility = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            List<CardEffect> effects = triggeredEffects(gameData, ability, context);
            if (effects != null) {
                byAbility.add(effects);
            }
        }
        return byAbility;
    }

    /**
     * The effects {@code ability} puts on the stack as it triggers now, or {@code null} when it doesn't trigger: its
     * event condition (CR 603.2) or intervening "if" (CR 603.4) is false in {@code context}, or "this ability triggers
     * only once" was used up by its source (the context's source permanent, or else its source card). Triggering marks
     * that limit. The intervening "if" is wrapped around each effect for the check as the ability resolves. No step
     * ability is written with another limit, or to trigger once for several occurrences, and this throws on one.
     */
    public List<CardEffect> triggeredEffects(GameData gameData, TriggeredAbility ability, ConditionContext context) {
        TriggerOptions options = ability.options();
        if (options.multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("A step or phase begins once; no step reader reads "
                    + options.multiplicity() + ": " + ability);
        }
        if (options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE) {
            throw new IllegalStateException("No step reader reads the limit " + options.limit() + ": " + ability);
        }
        if (options.eventCondition() != null
                && !conditionEvaluationService.isMet(gameData, options.eventCondition(), context)) {
            return null;
        }
        Condition interveningIf = options.interveningIf();
        if (interveningIf != null && !conditionEvaluationService.isMet(gameData, interveningIf, context)) {
            return null;
        }
        if (options.limit() == TriggerLimit.ONCE) {
            UUID sourceId = context.sourcePermanentId() != null ? context.sourcePermanentId()
                    : context.sourceCard().getId();
            if (TriggeredAbilityStackService.limitReached(gameData, TriggerLimit.ONCE, sourceId, ability.limitKey())) {
                return null;
            }
            TriggeredAbilityStackService.markLimit(gameData, TriggerLimit.ONCE, sourceId, ability.limitKey());
        }
        List<CardEffect> effects = new ArrayList<>(ability.effects().size());
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = registration.effect();
            effects.add(interveningIf == null ? effect : new ConditionalEffect(interveningIf, effect, true));
        }
        return effects;
    }
}
