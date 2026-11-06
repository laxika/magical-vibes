package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "Distribute N {@code counterType} counters among any number of target creatures [you control]"
 * on a death trigger: Jugan, the Rising Star (five, any creatures), Quirion Beastcaller (the
 * +1/+1 counters on it, creatures you control), Grand Ossuary (the dying creature's power, creatures
 * its controller controls).
 * <p>
 * The targets and the division are announced as the ability goes on the stack (CR 601.2c-d,
 * CR 603.3d): {@code StackedDividedDamageTriggerService} evaluates {@code total} against the stack
 * entry (so {@code CountersOnTriggeringPermanent} and {@code TriggeringPermanentPower} read the dying
 * creature as it last existed), asks the controller for one or more targets and the amount on each,
 * and records the division on the entry. A division staged in {@code pendingETBDamageAssignments}
 * is taken over instead of asking, once it is checked. Resolution places the announced counters on
 * the recipients that are still legal targets (CR 608.2b). A "you may" is a {@link MayEffect} around
 * this effect on the card (CR 603.5).
 *
 * @param counterType the counter type placed on the recipients
 * @param total       the number of counters to distribute, evaluated against the stack entry
 * @param recipients  which creatures may be chosen, and who chooses them
 */
public record DistributeCountersAmongCreaturesOnDeathEffect(
        CounterType counterType, DynamicAmount total, Recipients recipients) implements CardEffect {

    /** Which creatures the counters may be distributed among. */
    public enum Recipients {
        /** Any creature on the battlefield (Jugan). */
        ANY_CREATURE,
        /** Creatures the ability's controller controls (Quirion Beastcaller). */
        CREATURES_YOU_CONTROL,
        /**
         * Creatures the dying creature's controller controls. That player chooses the targets and the
         * division; the ability stays under its source's controller (Grand Ossuary's "its controller
         * distributes … among any number of target creatures they control", under the planar
         * controller).
         */
        CREATURES_ITS_CONTROLLER_CONTROLS
    }

    /** "Distribute {@code total} counters among any number of target creatures" (Jugan). */
    public static DistributeCountersAmongCreaturesOnDeathEffect amongAnyTargetCreatures(
            CounterType counterType, DynamicAmount total) {
        return new DistributeCountersAmongCreaturesOnDeathEffect(counterType, total, Recipients.ANY_CREATURE);
    }

    /** "Distribute {@code total} counters among any number of target creatures you control" (Quirion Beastcaller). */
    public static DistributeCountersAmongCreaturesOnDeathEffect amongTargetCreaturesYouControl(
            CounterType counterType, DynamicAmount total) {
        return new DistributeCountersAmongCreaturesOnDeathEffect(counterType, total, Recipients.CREATURES_YOU_CONTROL);
    }

    /** "Its controller distributes {@code total} counters among any number of target creatures they control" (Grand Ossuary). */
    public static DistributeCountersAmongCreaturesOnDeathEffect amongTargetCreaturesItsControllerControls(
            CounterType counterType, DynamicAmount total) {
        return new DistributeCountersAmongCreaturesOnDeathEffect(
                counterType, total, Recipients.CREATURES_ITS_CONTROLLER_CONTROLS);
    }

    /** Whether only creatures controlled by the distributing player may receive counters. */
    public boolean controlledCreaturesOnly() {
        return recipients != Recipients.ANY_CREATURE;
    }

    @Override
    public TargetSpec targetSpec() {
        // The targets are announced with the division on the stack entry, outside the targeting
        // pipeline, like divided damage announced on a stacked trigger.
        return TargetSpec.NONE;
    }
}
