package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * Rider for {@link DestroyEachTargetPermanentEffect}: "~ deals damage to each player equal to the
 * number of permanents they controlled that were put into a graveyard this way."
 *
 * <p>Unlike {@code MassDamageEffect(new EventValue(), true)} — which deals the single total to
 * everyone (Volcanic Eruption) — the amount here is per player. The tally is read from the
 * {@code StackEntry.eventPlayerIds} channel the destroy handler stamps with the controller of every
 * permanent actually put into a graveyard, so a player who lost three artifacts takes 3 while a
 * player who lost none takes nothing (and indestructible / regenerated permanents contribute
 * nothing). Builder's Bane.
 */
public record DealDamageToEachDestroyedPermanentControllerEffect() implements DamageDealingEffect {

    @Override
    public DynamicAmount damageAmount() {
        return new EventValue();
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
