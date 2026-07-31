package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * On resolution, prompts the controller to choose a color, then grants every creature they
 * control matching {@code filter} protection from that color until end of turn. A single color
 * is chosen and applied to all of them; the set of creatures is determined on resolution.
 * <p>
 * Untargeted — pass {@code null} for "each creature you control". Used by Brave the Elements
 * ("Choose a color. White creatures you control gain protection from the chosen color until end
 * of turn") with a {@code PermanentColorInPredicate} of white.
 */
public record GrantProtectionChoiceToOwnCreaturesUntilEndOfTurnEffect(PermanentPredicate filter) implements CardEffect {
}
