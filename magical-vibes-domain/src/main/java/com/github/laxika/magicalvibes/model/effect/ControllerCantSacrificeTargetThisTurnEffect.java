package com.github.laxika.magicalvibes.model.effect;

/**
 * The effect's controller can't sacrifice the target permanent this turn. The restriction belongs
 * to that player rather than the permanent, so another controller may still sacrifice it and
 * ability removal doesn't end it (Call for Aid: "You can't sacrifice those creatures this turn").
 */
public record ControllerCantSacrificeTargetThisTurnEffect() implements CardEffect {
}
