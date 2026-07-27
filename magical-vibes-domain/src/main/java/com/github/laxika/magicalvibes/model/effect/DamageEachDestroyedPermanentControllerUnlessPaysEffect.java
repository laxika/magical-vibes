package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Rider for {@link DestroyAllPermanentsEffect}: "For each permanent destroyed this way, ~ deals
 * {@code damage} damage to that permanent's controller unless they pay {@code manaCost}."
 *
 * <p>One independent choice per destroyed permanent, not per player — a player who lost three lands
 * is asked three times and pays three times to avoid all of it. The payers are read from the
 * {@code StackEntry.eventPlayerIds} channel the destroy-all handler stamps with the controller of
 * every permanent actually destroyed (indestructible / regenerated permanents are not there, so they
 * produce no prompt). Stench of Evil ({@code 1, "{2}"}).
 *
 * <p>Routed through the damage system so prevention/redirection/infect apply.
 */
public record DamageEachDestroyedPermanentControllerUnlessPaysEffect(int damage, String manaCost)
        implements DamageDealingEffect {

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(damage);
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
