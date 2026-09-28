package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller secretly chooses a creature or planeswalker controlled by the target player.
 * That player sacrifices a creature or planeswalker of their choice, then the chosen permanent
 * is sacrificed.
 */
public record TargetPlayerChoosesCreatureOrPlaneswalkerThenSacrificesChosenPermanentEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
