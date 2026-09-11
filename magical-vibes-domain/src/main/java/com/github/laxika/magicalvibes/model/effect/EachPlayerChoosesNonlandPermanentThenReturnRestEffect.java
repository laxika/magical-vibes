package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses one nonland permanent they control, then all other nonland permanents are
 * returned to their owners' hands.
 *
 * <p>Choices are made in active-player order before any permanent is returned.
 */
public record EachPlayerChoosesNonlandPermanentThenReturnRestEffect() implements CardEffect {
}
