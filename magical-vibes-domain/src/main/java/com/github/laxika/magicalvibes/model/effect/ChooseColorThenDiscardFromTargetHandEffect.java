package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a color, then chooses one card of that color from the target player's
 * revealed hand for that player to discard.
 */
public record ChooseColorThenDiscardFromTargetHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
