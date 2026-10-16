package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The source equipment's equipped creature gets +powerBoost/+toughnessBoost until end of turn.
 * If the source equipment is not attached to a creature, the effect does nothing.
 *
 * <p>The dynamic amounts are evaluated relative to the equipped creature at resolution time, so a
 * {@link com.github.laxika.magicalvibes.model.amount.PermanentCount} counting attacking creatures
 * resolves correctly. Used to model equipment-granted triggered abilities that give the equipped
 * creature a (possibly dynamic) temporary boost, e.g. Veteran's Armaments' "Whenever this creature
 * attacks or blocks, it gets +1/+1 until end of turn for each attacking creature" (placed in the
 * {@code ON_ATTACK} and {@code ON_BLOCK} slots on the equipment). For a fixed boost that also grants
 * a keyword see {@link BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect}.
 *
 * <p>When {@code orControlledCreatureMatching} is set the effect reads "equipped creature or a
 * creature you control matching ... gets ...": at resolution the candidates are the equipped
 * creature plus the controller's matching creatures (each counted once), and the controller
 * chooses one when there are several (Hand of Vecna). Without an attached creature the source
 * can still boost a matching creature, which is how a token that copied the trigger works.
 */
public record BoostEquippedCreatureUntilEndOfTurnEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost,
        PermanentPredicate orControlledCreatureMatching
) implements CardEffect {

    public BoostEquippedCreatureUntilEndOfTurnEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, null);
    }

    /**
     * The boost is applied to the attached creature, so a sacrifice cost on the same ability
     * ("Sacrifice this Aura: Enchanted creature gets +3/+3 until end of turn." — Briar Shield)
     * must capture that creature at activation time, before the attachment is severed.
     */
    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return true;
    }

    @Override
    public boolean hasAbilityResolutionCondition() {
        return powerBoost.requiresAbilityResolutionCount()
                || toughnessBoost.requiresAbilityResolutionCount();
    }
}
