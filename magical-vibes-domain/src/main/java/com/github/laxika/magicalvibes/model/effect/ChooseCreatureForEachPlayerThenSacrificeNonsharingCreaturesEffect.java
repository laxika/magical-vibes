package com.github.laxika.magicalvibes.model.effect;

/**
 * For each player, the resolving spell's controller chooses one creature that player controls.
 * Then each player sacrifices every other creature they control that does not share a creature type
 * with their chosen creature.
 *
 * <p>The choices are made before any of the sacrifices happen, and the sacrifices are performed
 * together by {@code ChooseCreatureForEachPlayerThenSacrificeNonsharingCreaturesEffectHandler}.
 */
public record ChooseCreatureForEachPlayerThenSacrificeNonsharingCreaturesEffect() implements CardEffect {
}
