package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.TriggeredAbility;

import java.util.List;

/**
 * Whether a triggered ability triggers as its trigger event occurs, judged from its options: its intervening "if"
 * holds (CR 603.4) and its event condition holds (CR 603.2), each read against the collector's context
 * ({@code ConditionEvaluationService.judge}). The readers' effect lists for a collector take one, so the effects of an
 * ability that doesn't trigger are left out; the intervening "if" still wraps the effects of one that does, so it is
 * checked again as the ability resolves.
 */
@FunctionalInterface
public interface TriggerJudge {

    /** Judges nothing: for code that inspects the abilities' effects rather than triggering them. */
    TriggerJudge NONE = ability -> true;

    /** Whether {@code ability}'s intervening "if" and event condition hold. */
    boolean triggers(TriggeredAbility ability);

    /** The abilities of {@code abilities} that trigger, in order. */
    default List<TriggeredAbility> filter(List<TriggeredAbility> abilities) {
        return abilities.stream().filter(this::triggers).toList();
    }
}
