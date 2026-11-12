package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reads the "at the beginning of [step or phase]" abilities (CR 603.2b) for the step and phase readers
 * ({@link StepTriggerService}, the planar step triggers): an ability's trigger event is the step's beginning when its
 * event condition (CR 603.2) holds in the reader's {@link ConditionContext} (its source, its controller and, for "that
 * player", the player whose step it is). The stack door judges the rest as it puts the ability on the stack.
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
     * The abilities of {@code abilities} whose trigger event this step or phase beginning is, in order: those whose event
     * condition (CR 603.2) holds in {@code context}. The reader hands each one whole to the stack door
     * ({@code TriggeredAbilityStackService.put}), which judges its intervening "if" (CR 603.4) and "this ability
     * triggers only once" and puts it on the stack as one object (CR 603.3). A step or phase begins once, so no step
     * ability is written to trigger once for several occurrences or with a limit other than "only once"; this throws on
     * one.
     */
    public List<TriggeredAbility> triggering(GameData gameData, List<TriggeredAbility> abilities,
                                             ConditionContext context) {
        List<TriggeredAbility> triggering = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            TriggerOptions options = ability.options();
            if (options.multiplicity() != TriggerMultiplicity.EACH) {
                throw new IllegalStateException("A step or phase begins once; no step reader reads "
                        + options.multiplicity() + ": " + ability);
            }
            if (options.limit() != TriggerLimit.NONE && options.limit() != TriggerLimit.ONCE) {
                throw new IllegalStateException("No step reader reads the limit " + options.limit() + ": " + ability);
            }
            if (conditionEvaluationService.eventConditionMet(gameData, options, context)) {
                triggering.add(ability);
            }
        }
        return triggering;
    }
}
