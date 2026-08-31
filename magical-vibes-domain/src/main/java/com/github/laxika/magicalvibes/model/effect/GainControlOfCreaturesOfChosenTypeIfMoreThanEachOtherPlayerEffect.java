package com.github.laxika.magicalvibes.model.effect;

/**
 * Choose a creature type. If the controller controls more creatures of that type than each other
 * player, they gain permanent control of all creatures of that type (Peer Pressure).
 *
 * <p>The creature type is chosen during resolution and the count comparison is made before any
 * control changes are applied.</p>
 */
public record GainControlOfCreaturesOfChosenTypeIfMoreThanEachOtherPlayerEffect() implements CardEffect {
}
