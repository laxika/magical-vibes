package com.github.laxika.magicalvibes.model.effect;

/** Goads each creature controlled by the targeted player until the controller's next turn. */
public record GoadCreaturesTargetPlayerControlsUntilNextTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
