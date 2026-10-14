package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

/**
 * Resolves Gandalf, Westward Voyager's spell-copy or controller-draw trigger. {@code spellSnapshot} is the
 * triggering spell as it was when the ability triggered, so the copy is still made after that spell has
 * been countered.
 */
public record GandalfWestwardVoyagerEffect(StackEntry spellSnapshot) implements TriggeringSpellReferencingEffect {

    public GandalfWestwardVoyagerEffect() {
        this(null);
    }
}
