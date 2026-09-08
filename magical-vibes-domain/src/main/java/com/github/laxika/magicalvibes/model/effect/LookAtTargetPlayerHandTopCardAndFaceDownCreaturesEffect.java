package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller looks at a target player's hand, library top card, and face-down creatures they
 * control.
 */
public record LookAtTargetPlayerHandTopCardAndFaceDownCreaturesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
