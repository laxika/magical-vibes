package com.github.laxika.magicalvibes.model.effect;

/** Exchanges a targeted card the controller owns in the ante with the top card of their library. */
public record ExchangeTargetAnteCardWithTopOfLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.exileCard());
    }
}
