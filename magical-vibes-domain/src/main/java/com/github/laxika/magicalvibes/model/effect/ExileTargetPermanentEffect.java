package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the targeted permanent (also handles multi-target via targetIds).
 * Optionally creates a token for the exiled permanent's controller
 * (e.g. Crib Swap creates a 1/1 Shapeshifter for the target's controller).
 *
 * @param tokenForController if non-null, creates this token for each exiled permanent's controller
 */
public record ExileTargetPermanentEffect(CreateTokenEffect tokenForController) implements CardEffect {

    public ExileTargetPermanentEffect() {
        this(null);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
