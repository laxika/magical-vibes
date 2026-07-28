package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Taps every untapped permanent matching {@code filter} that the end-step player
 * ({@code entry.getTargetId()}) controls, then deals damage to that player equal to the number
 * of permanents tapped this way. Used by Monsoon ("At the beginning of each player's end step,
 * tap all untapped Islands that player controls and this enchantment deals X damage to the
 * player, where X is the number of Islands tapped this way").
 *
 * <p>Only permanents that were untapped before the effect resolved count toward the damage —
 * already-tapped ones are neither tapped again nor counted.
 */
public record TapPlayersPermanentsAndDamageEqualToCountEffect(PermanentPredicate filter)
        implements EndStepPlayerTargetedEffect {
}
