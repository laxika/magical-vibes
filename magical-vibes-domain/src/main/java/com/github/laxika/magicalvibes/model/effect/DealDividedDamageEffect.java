package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * "Deals N damage divided … among … targets." — the unified divided/multi-target damage effect.
 *
 * <p>Collapses the former divided-damage family (Deal{Divided,Ordered,XDivided…}Damage… records)
 * onto a single record parameterised by {@link DivisionMode}:
 *
 * <ul>
 *   <li>{@code CHOSEN} — controller-announced division. Per-target amounts come from
 *       {@code StackEntry.damageAssignments} (Ignite Disorder, Fight with Fire kicked, Hail of
 *       Arrows, Huatli's −X), or from {@code GameData.pendingETBDamageAssignments} when
 *       {@link #etbAssignments} is set (Inferno Titan, Bogardan Hellkite ETB).</li>
 *   <li>{@code EVEN} — {@code floor(totalDamage / targetCount)} to each target (Fireball).</li>
 *   <li>{@code ORDERED} — fixed {@link #orderedAmounts} assigned by target order (Cone of Flame,
 *       Arc Trail).</li>
 * </ul>
 *
 * @param totalDamage       the total to divide (CHOSEN/EVEN). Ignored for ORDERED (pass {@code null}).
 * @param orderedAmounts    fixed per-target amounts (ORDERED only; {@code null} otherwise).
 * @param mode              how the total is split.
 * @param targetRestriction target legality restriction (informational; {@code null} = any).
 * @param maxTargets        maximum number of targets, {@code 0} = unbounded.
 * @param canTargetPlayers  whether players may be targeted (creatures and/or players).
 * @param damagedCreaturesCantBlock    if {@code true}, creatures dealt damage this way can't block this turn.
 * @param etbAssignments    if {@code true} (CHOSEN only), the assignments come from
 *                          {@code GameData.pendingETBDamageAssignments} and resolve outside the
 *                          standard targeting pipeline (ETB/attack divided damage).
 */
public record DealDividedDamageEffect(
        DynamicAmount totalDamage,
        List<Integer> orderedAmounts,
        DivisionMode mode,
        PermanentPredicate targetRestriction,
        int maxTargets,
        boolean canTargetPlayers,
        boolean damagedCreaturesCantBlock,
        boolean etbAssignments) implements CardEffect {

    /** Fixed total divided as you choose among any number of targets (creatures and/or players). */
    public static DealDividedDamageEffect chosenAmongAnyTargets(int totalDamage) {
        return new DealDividedDamageEffect(
                new Fixed(totalDamage), null, DivisionMode.CHOSEN, null, 0, true, false, false);
    }

    /**
     * Dynamic total divided as you choose among any number of targets (creatures and/or players),
     * where the total is computed from game state at cast time (Jaws of Stone — "X is the number of
     * Mountains you control as you cast this spell").
     */
    public static DealDividedDamageEffect chosenAmongAnyTargets(DynamicAmount totalDamage) {
        return new DealDividedDamageEffect(
                totalDamage, null, DivisionMode.CHOSEN, null, 0, true, false, false);
    }

    /** Fixed total divided as you choose among up to {@code maxTargets} target creatures. */
    public static DealDividedDamageEffect chosenAmongTargetCreatures(int totalDamage) {
        return new DealDividedDamageEffect(
                new Fixed(totalDamage), null, DivisionMode.CHOSEN,
                new PermanentIsCreaturePredicate(), 0, false, false, false);
    }

    /**
     * Fixed total divided as you choose among any number of targets, resolved from the ETB
     * assignment buffer (Inferno Titan / Bogardan Hellkite). {@code maxTargets} bounds the split.
     */
    public static DealDividedDamageEffect chosenAmongAnyTargetsEtb(int totalDamage, int maxTargets) {
        return new DealDividedDamageEffect(
                new Fixed(totalDamage), null, DivisionMode.CHOSEN, null, maxTargets, false, false, true);
    }

    /** X damage divided as you choose among any number of target attacking creatures. */
    public static DealDividedDamageEffect xAmongAttackingCreatures() {
        return new DealDividedDamageEffect(
                new XValue(), null, DivisionMode.CHOSEN,
                new PermanentIsAttackingPredicate(), 0, false, false, false);
    }

    /**
     * X damage divided as you choose among any number of target creatures; creatures dealt damage
     * this way can't block this turn (Huatli, Warrior Poet).
     */
    public static DealDividedDamageEffect xAmongTargetCreaturesCantBlock() {
        return new DealDividedDamageEffect(
                new XValue(), null, DivisionMode.CHOSEN,
                new PermanentIsCreaturePredicate(), 0, false, true, false);
    }

    /** X damage divided evenly (rounded down) among any number of targets (Fireball). */
    public static DealDividedDamageEffect xDividedEvenly() {
        return new DealDividedDamageEffect(
                new XValue(), null, DivisionMode.EVEN, null, 0, true, false, false);
    }

    /** Fixed damage assigned to targets in order (Cone of Flame, Arc Trail). */
    public static DealDividedDamageEffect ordered(List<Integer> orderedAmounts) {
        return new DealDividedDamageEffect(
                null, orderedAmounts, DivisionMode.ORDERED, null, 0, true, false, false);
    }

    @Override
    public TargetSpec targetSpec() {
        // ETB divided damage collects its targets through GameData.pendingETBDamageAssignments,
        // bypassing the standard targeting system — no spec-level targeting.
        if (etbAssignments) {
            return TargetSpec.NONE;
        }
        // PLAYER_OR_PERMANENT is a no-op in the spec interpreter, which preserves this effect's
        // null-targetId tolerance (CHOSEN-mode targets ride on StackEntry.damageAssignments). The
        // kept @ValidatesTarget validator (DamageTargetValidators) enforces the real per-target type
        // rules, including rejecting player targets when this spell targets creatures only.
        return TargetSpec.harmful(TargetCategory.PLAYER_OR_PERMANENT);
    }
}
