package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "All &lt;permanents&gt; have 'At the beginning of your upkeep, sacrifice this permanent unless you
 * pay {N}.'" — Energy Flux (artifacts, {2}), Pendrell Mists (creatures, {1}).
 *
 * <p>An empty {@code manaCost} with a positive {@code lifeAmount} represents a life-only payment.
 *
 * <p>A static marker placed in {@link com.github.laxika.magicalvibes.model.EffectSlot#STATIC}
 * and read by {@code StepTriggerService} during each player's upkeep: for every permanent the
 * active player controls that matches {@code filter} it pushes a
 * {@link ForcedCostOrElseEffect}{@code (PayManaCost, [SacrificeSelfEffect], true)} trigger sourced
 * at that permanent. The grant is global (every matching permanent, regardless of who controls the
 * enchantment) and the trigger fires on each permanent's controller's own upkeep. Several grants
 * stack: a permanent matching two of them gets one trigger per grant.
 *
 * <p>Not part of the layer-system board computation (no {@code LayerClassifier} entry), so the
 * layer system safely ignores it.
 *
 * @param filter      which permanents receive the granted ability
 * @param manaCost   the mana cost that must be paid to avoid the sacrifice; empty for life-only
 * @param lifeAmount the life paid alongside the mana cost
 */
public record AllPermanentsUpkeepSacrificeUnlessPayEffect(
        PermanentPredicate filter, String manaCost, int lifeAmount) implements CardEffect {

    public AllPermanentsUpkeepSacrificeUnlessPayEffect(PermanentPredicate filter, String manaCost) {
        this(filter, manaCost, 0);
    }

    /** "Unless you pay N life" rather than a mana cost. */
    public AllPermanentsUpkeepSacrificeUnlessPayEffect(PermanentPredicate filter, int lifeAmount) {
        this(filter, "", lifeAmount);
    }
}
