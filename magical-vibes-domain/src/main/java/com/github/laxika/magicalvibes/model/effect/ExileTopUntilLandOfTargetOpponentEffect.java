package com.github.laxika.magicalvibes.model.effect;

/** Target opponent exiles cards from the top of their library until they exile a land card. */
public record ExileTopUntilLandOfTargetOpponentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
