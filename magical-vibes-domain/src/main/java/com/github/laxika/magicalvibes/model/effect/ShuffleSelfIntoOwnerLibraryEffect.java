package com.github.laxika.magicalvibes.model.effect;

/**
 * The source permanent's owner shuffles it into their library.
 * Non-targeted self effect (e.g. Blitz Hellion's end-step trigger); acts on the
 * stack entry's source permanent.
 */
public record ShuffleSelfIntoOwnerLibraryEffect() implements CardEffect {
}
