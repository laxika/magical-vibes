package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, prompts the controller to choose a basic land type, then adds that type
 * to the target land "in addition to its other types".
 * The target land also gains the intrinsic mana ability of the chosen type.
 *
 * <p>Used by Navigator's Compass ({@link EffectDuration#UNTIL_END_OF_TURN}).
 *
 * @param duration how long the granted type lasts
 */
public record GrantBasicLandTypeToTargetEffect(EffectDuration duration) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
