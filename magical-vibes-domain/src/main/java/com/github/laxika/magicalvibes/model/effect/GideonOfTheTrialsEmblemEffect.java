package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates Gideon of the Trials' emblem:
 * "As long as you control a Gideon planeswalker, you can't lose the game and your opponents
 * can't win the game."
 *
 * <p>The resolving handler builds an {@link com.github.laxika.magicalvibes.model.Emblem} whose
 * static effect is a {@link ConditionalEffect} wrapping {@link CantLoseGameEffect}, gated on the
 * emblem's controller controlling a Gideon planeswalker. The loss/win gate reads that emblem in
 * {@code GameQueryService.canPlayerLoseGame}.
 */
public record GideonOfTheTrialsEmblemEffect() implements CardEffect {
}
