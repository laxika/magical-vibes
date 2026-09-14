package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller and a target opponent each choose a creature that player controls, then the
 * chosen creatures are sacrificed simultaneously.
 */
public record ControllerAndTargetPlayerChooseCreaturesThenSacrificeEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
