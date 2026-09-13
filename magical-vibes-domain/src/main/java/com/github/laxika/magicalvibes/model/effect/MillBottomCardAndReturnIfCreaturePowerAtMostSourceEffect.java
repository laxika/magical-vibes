package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts the controller's bottom library card into their graveyard, then returns that same card to
 * the battlefield if it is a creature card whose power is no greater than the source's power.
 */
public record MillBottomCardAndReturnIfCreaturePowerAtMostSourceEffect() implements CardEffect {
}
