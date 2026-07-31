package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "Distribute N {@code counterType} counters among … target creatures." — the counter analogue of
 * {@link DealDividedDamageEffect}, parameterised by {@link DivisionMode}.
 *
 * <ul>
 *   <li>{@code EVEN} — the counters are split evenly across the effect's chosen target group
 *       ({@code floor(total / targetCount)} on each), which is exactly the forced distribution for
 *       the "distribute two counters among one or two target creatures" family (Splendid Agony's two
 *       -1/-1 counters): CR 601.2d requires each target to receive at least one counter, so with a
 *       total of two and at most two targets the split is fully determined. The target group (count
 *       and legality) is declared on the card via {@code target(filter, min, max)}; the handler
 *       reads {@code StackEntry.targetsForEffect(this)}. Do <em>not</em> use this mode for a genuine
 *       three-or-more distribution where the controller has a real choice of how to divide.</li>
 *   <li>{@code CHOSEN} — the controller announces the division as the spell is put on the stack
 *       (CR 601.2d); per-target amounts ride on {@code StackEntry.damageAssignments}, exactly like
 *       divided damage. Use for a real "distribute X counters among any number of target creatures"
 *       (Spoils of War), where {@code total} is evaluated from game state at cast time.</li>
 * </ul>
 *
 * @param counterType           the counter to place.
 * @param total                 the total number of counters to distribute.
 * @param mode                  how the total is split among the targets.
 * @param removeAtNextCleanup   when {@code true}, every counter placed is scheduled to be removed
 *                              from that same creature at the beginning of the next cleanup step
 *                              ("For each +1/+1 counter you put on a creature this way, remove a
 *                              +1/+1 counter from that creature at the beginning of the next
 *                              cleanup step" — Bounty of the Hunt), making the boost effectively
 *                              last only for the turn.
 */
public record DistributeCountersAmongTargetsEffect(
        CounterType counterType, DynamicAmount total, DivisionMode mode, boolean removeAtNextCleanup)
        implements CardEffect {

    /** Fixed total split evenly across a {@code target(filter, 1, 2)} group (Splendid Agony). */
    public static DistributeCountersAmongTargetsEffect evenlyAmongTargets(CounterType counterType, int total) {
        return new DistributeCountersAmongTargetsEffect(counterType, new Fixed(total), DivisionMode.EVEN, false);
    }

    /**
     * Fixed total distributed as the controller chooses among the announced target creatures, with
     * every placed counter removed again at the beginning of the next cleanup step (Bounty of the
     * Hunt — "Distribute three +1/+1 counters among one, two, or three target creatures").
     */
    public static DistributeCountersAmongTargetsEffect chosenUntilNextCleanup(CounterType counterType, int total) {
        return new DistributeCountersAmongTargetsEffect(counterType, new Fixed(total), DivisionMode.CHOSEN, true);
    }

    /**
     * Dynamic total distributed as the controller chooses among any number of target creatures, with
     * the total computed from game state at cast time (Spoils of War — "X is the number of artifact
     * and/or creature cards in an opponent's graveyard as you cast this spell").
     */
    public static DistributeCountersAmongTargetsEffect chosenAmongTargetCreatures(
            CounterType counterType, DynamicAmount total) {
        return new DistributeCountersAmongTargetsEffect(counterType, total, DivisionMode.CHOSEN, false);
    }

    @Override
    public TargetSpec targetSpec() {
        boolean harmful = counterType == CounterType.MINUS_ONE_MINUS_ONE
                || counterType == CounterType.MINUS_TWO_MINUS_ONE;
        // CHOSEN-mode targets ride on StackEntry.damageAssignments, so targetId is null on that path.
        // PLAYER_OR_PERMANENT is a no-op in the spec interpreter, which preserves that null tolerance;
        // the kept @ValidatesTarget validator (CreatureModTargetValidators) enforces creature-only
        // legality, as does the cast-time assignment loop in SpellCastingService.
        TargetCategory category = mode == DivisionMode.CHOSEN
                ? TargetCategory.PLAYER_OR_PERMANENT
                : TargetCategory.CREATURE;
        return harmful ? TargetSpec.harmful(category) : TargetSpec.benign(category);
    }
}
