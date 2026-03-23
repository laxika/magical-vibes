package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: prevents casting spells with the same name as the card imprinted
 * on the source permanent.
 *
 * @param opponentsOnly if true, only opponents of the source's controller are restricted
 *                      (e.g. Ixalan's Binding); if false, all players are restricted
 *                      (e.g. Exclusion Ritual)
 */
public record CantCastSpellsWithSameNameAsExiledCardEffect(boolean opponentsOnly) implements CardEffect {

    /** Default constructor — restricts all players. */
    public CantCastSpellsWithSameNameAsExiledCardEffect() {
        this(false);
    }
}
