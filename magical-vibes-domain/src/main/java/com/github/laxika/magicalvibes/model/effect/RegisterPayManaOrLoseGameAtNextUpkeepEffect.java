package com.github.laxika.magicalvibes.model.effect;

/**
 * SPELL delayed-trigger registrar for an upkeep payment that causes the resolving spell's
 * controller to lose the game if they do not pay.
 *
 * @param manaCost mana cost the spell's controller may pay at their next upkeep
 */
public record RegisterPayManaOrLoseGameAtNextUpkeepEffect(String manaCost) implements CardEffect {
}
