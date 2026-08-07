package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Adds mana of the given color to the active player's mana pool ("that player adds {G}{G}" on a
 * trigger that fires on every player's turn). The recipient is the stack entry's target — the
 * active player recorded when the trigger was put on the stack — not the source's controller.
 * Not a {@link ManaProducingEffect}: a beginning-of-step trigger uses the stack (CR 605.1b) and
 * therefore is not a mana ability.
 */
public record AwardManaToActivePlayerEffect(ManaColor color, int amount) implements CardEffect {
}
