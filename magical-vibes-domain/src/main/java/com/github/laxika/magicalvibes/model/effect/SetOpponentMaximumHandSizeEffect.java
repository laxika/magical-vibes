package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the chosen opponent's maximum hand size is set to a specific value
 * (Cursed Rack — four). Modeled, like Booby Trap's chosen opponent, as applying to the
 * controller's opponent(s).
 *
 * <p>A "set" value overrides the base/reduced value; when several hand-size effects apply
 * they resolve in timestamp order (CR 402.2), and any "no maximum hand size" effect still
 * wins (handled in {@code TurnCleanupService.hasNoMaximumHandSize}). Checked during the
 * cleanup discard step.
 */
public record SetOpponentMaximumHandSizeEffect(int maximumHandSize) implements OpponentMaxHandSizeEffect {

    @Override
    public int applyToMaximumHandSize(int currentMax) {
        return maximumHandSize;
    }
}
