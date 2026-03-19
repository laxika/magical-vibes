package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys one permanent at random from the list of targets stored in
 * {@code StackEntry.targetIds}.
 *
 * <p>Used by Capricious Efreet's upkeep trigger: "choose target nonland
 * permanent you control and up to two target nonland permanents you don't
 * control. Destroy one of them at random."
 */
public record DestroyOneOfTargetsAtRandomEffect() implements CardEffect {
}
