package com.github.laxika.magicalvibes.model.effect;

/**
 * Mana Echoes: "add {C} for each creature you control that shares a creature type with it", where "it" is the
 * creature whose entering triggered the ability, read as the ability resolves (CR 608.2h): the creature on the
 * battlefield, else as it last existed there.
 */
public record ManaEchoesEffect() implements CardEffect {
}
