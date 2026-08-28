package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a creature controlled by an opponent, then other creatures get the
 * specified power and toughness modifiers until end of turn.
 */
public record ChooseOpponentCreatureThenBoostOthersEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {
}
