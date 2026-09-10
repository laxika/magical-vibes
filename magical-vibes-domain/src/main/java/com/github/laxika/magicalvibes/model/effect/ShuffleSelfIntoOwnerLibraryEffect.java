package com.github.laxika.magicalvibes.model.effect;

/**
 * The source permanent's owner shuffles it into their library, optionally followed by an existing
 * effect resolved for the source permanent's controller or owner.
 * Non-targeted self effect (e.g. Blitz Hellion's end-step trigger); acts on the
 * stack entry's source permanent.
 */
public record ShuffleSelfIntoOwnerLibraryEffect(
        CardEffect thenEffect,
        ThenEffectRecipient recipient
) implements CardEffect {

    public ShuffleSelfIntoOwnerLibraryEffect() {
        this(null, null);
    }
}
