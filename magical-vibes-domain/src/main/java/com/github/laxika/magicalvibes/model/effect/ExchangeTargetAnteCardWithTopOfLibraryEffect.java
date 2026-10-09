package com.github.laxika.magicalvibes.model.effect;

/** Grants ownership of a targeted card in the ante, then exchanges it with the top card of the controller's library. */
public record ExchangeTargetAnteCardWithTopOfLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.exileCard());
    }
}
