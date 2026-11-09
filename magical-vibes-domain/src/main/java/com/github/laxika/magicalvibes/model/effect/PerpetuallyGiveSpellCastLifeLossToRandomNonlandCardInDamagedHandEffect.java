package com.github.laxika.magicalvibes.model.effect;

/**
 * Perpetually gives a random nonland card in the combat-damaged player's hand a trigger that
 * makes its controller lose 2 life when it is cast.
 */
public record PerpetuallyGiveSpellCastLifeLossToRandomNonlandCardInDamagedHandEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
