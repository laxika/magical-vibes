package com.github.laxika.magicalvibes.model.effect;

/** Adds mana represented by an enchanted permanent's mana cost, or colored symbols of a milled cost card. */
public record AddManaEqualToEnchantedPermanentManaCostEffect(boolean coloredSymbolsOfMilledCostCards)
        implements ManaProducingEffect {

    public AddManaEqualToEnchantedPermanentManaCostEffect() {
        this(false);
    }
}
