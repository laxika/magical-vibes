package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "All &lt;permanents&gt; have 'At the beginning of your upkeep, sacrifice or destroy this permanent unless you
 * pay {N}.'" — Energy Flux (artifacts, {2}), Pendrell Mists (creatures, {1}).
 *
 * <p>An empty {@code manaCost} with a positive {@code lifeAmount} represents a life-only payment.
 *
 * <p>A static marker placed in {@link com.github.laxika.magicalvibes.model.EffectSlot#STATIC}
 * and read by {@code StepTriggerService} during each player's upkeep: for every permanent the
 * active player controls that matches {@code filter} it pushes a
 * {@link ForcedCostOrElseEffect}{@code (PayManaCost, [SacrificeSelfEffect] or
 * [DestroyReferencedPermanentEffect], true)} trigger sourced at that permanent. The grant is
 * global (every matching permanent, regardless of who controls the enchantment) and the trigger
 * fires on each permanent's controller's own upkeep. Several grants stack: a permanent matching
 * two of them gets one trigger per grant. When {@code excludeSource} is true, the permanent
 * carrying this grant is excluded from its own grant, for effects using wording such as "other
 * enchantments."
 *
 * <p>Not part of the layer-system board computation (no {@code LayerClassifier} entry), so the
 * layer system safely ignores it.
 *
 * @param filter      which permanents receive the granted ability
 * @param manaCost   the mana cost that must be paid to avoid the penalty; empty for life-only
 * @param lifeAmount   the life paid alongside the mana cost
 * @param excludeSource whether the permanent carrying this grant is excluded
 * @param sacrifice whether the unpaid penalty sacrifices rather than destroys the permanent
 */
public record AllPermanentsUpkeepSacrificeUnlessPayEffect(
        PermanentPredicate filter, String manaCost, int lifeAmount, boolean excludeSource, boolean sacrifice)
        implements CardEffect {

    public AllPermanentsUpkeepSacrificeUnlessPayEffect(PermanentPredicate filter, String manaCost) {
        this(filter, manaCost, 0, false, true);
    }

    public AllPermanentsUpkeepSacrificeUnlessPayEffect(PermanentPredicate filter, String manaCost,
                                                       boolean excludeSource) {
        this(filter, manaCost, 0, excludeSource, true);
    }

    /** "Unless you pay N life" rather than a mana cost. */
    public AllPermanentsUpkeepSacrificeUnlessPayEffect(PermanentPredicate filter, int lifeAmount) {
        this(filter, "", lifeAmount, false, true);
    }

    /** "Destroy each matching permanent unless its controller pays the mana cost." */
    public static AllPermanentsUpkeepSacrificeUnlessPayEffect destroy(PermanentPredicate filter, String manaCost) {
        return new AllPermanentsUpkeepSacrificeUnlessPayEffect(filter, manaCost, 0, false, false);
    }
}
