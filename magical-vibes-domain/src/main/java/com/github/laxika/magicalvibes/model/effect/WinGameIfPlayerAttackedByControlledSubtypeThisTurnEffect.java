package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * The controller wins if the player represented by the loss trigger was attacked this turn by
 * a permanent of the configured subtype that the controller controls.
 */
public record WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect(CardSubtype subtype)
        implements CardEffect {
}
