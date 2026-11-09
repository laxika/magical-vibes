package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolves Gandalf, Westward Voyager's spell-copy or controller-draw trigger. Once the triggering spell has left the
 * stack, the copy is made from the stack entry's triggering spell, the spell as it was when the ability triggered.
 */
public record GandalfWestwardVoyagerEffect() implements CardEffect {
}
