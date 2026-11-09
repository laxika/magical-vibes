package com.github.laxika.magicalvibes.model.effect;

/**
 * Doubles the life total of the enchanted creature's controller when used by an Aura's combat
 * damage trigger ("double its controller's life total", Celestial Mantle): the controller of the
 * creature the Aura enchants as the ability resolves, else of the creature that dealt the damage,
 * which the collector records as the stack entry's triggering permanent.
 */
public record DoubleEnchantedCreatureControllerLifeEffect() implements DoublingEffect {
}
