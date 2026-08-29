package com.github.laxika.magicalvibes.model.effect;

/**
 * Untaps the permanent put onto the battlefield by the preceding library-search effect on the
 * same stack entry. The permanent is identified by the stack entry's chosen-permanent link rather
 * than by a target.
 */
public record UntapSearchedPermanentEffect() implements CardEffect {
}
