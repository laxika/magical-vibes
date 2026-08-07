package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Reveals the top X cards of the controller's library (X is the stack entry's paid X value). Every
 * land card from among them is put onto the battlefield tapped and the rest are put on the bottom
 * of the library in a random order. When {@code untapCondition} is met at resolution, those lands
 * are untapped instead of staying tapped (Animist's Awakening's spell mastery rider).
 */
public record RevealTopXCardsLandsToBattlefieldTappedRestOnBottomRandomEffect(
        Condition untapCondition
) implements CardEffect {
}
