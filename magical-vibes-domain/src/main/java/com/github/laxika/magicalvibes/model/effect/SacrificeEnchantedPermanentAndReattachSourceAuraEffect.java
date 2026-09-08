package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Sacrifices the enchanted permanent, then moves the source Aura onto a matching permanent
 * controlled by that permanent's controller. Placed in a triggered slot and resolved by
 * {@code SacrificeEnchantedPermanentAndReattachSourceAuraEffectHandler}. The no-argument form is
 * used by Nettlevine Blight and permits creatures or lands.
 *
 * <p>The Aura never changes controller. If that player has no legal destination, the enchanted
 * permanent is still sacrificed and the now-unattached Aura is put into its owner's graveyard as a
 * state-based action.
 */
public record SacrificeEnchantedPermanentAndReattachSourceAuraEffect(PermanentPredicate destinationFilter)
        implements CardEffect {

    public SacrificeEnchantedPermanentAndReattachSourceAuraEffect() {
        this(new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsLandPredicate())));
    }
}
