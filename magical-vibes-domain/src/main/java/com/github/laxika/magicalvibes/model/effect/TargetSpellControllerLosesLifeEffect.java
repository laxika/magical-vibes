package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Makes the controller of the targeted spell or ability lose life.
 * Used as a companion effect alongside counter-spell effects (e.g. Psychic Barrier).
 * Does not independently target — piggybacks on the spell's existing target.
 */
public record TargetSpellControllerLosesLifeEffect(DynamicAmount amount) implements TriggeringSpellReferencingEffect {

    public TargetSpellControllerLosesLifeEffect(int amount) {
        this(new Fixed(amount));
    }
}
