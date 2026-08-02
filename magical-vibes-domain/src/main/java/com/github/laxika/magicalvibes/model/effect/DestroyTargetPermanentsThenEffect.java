package com.github.laxika.magicalvibes.model.effect;

/**
 * "Destroy each chosen target permanent, then [thenEffect]." The multi-target sibling of
 * {@link DestroyTargetPermanentThenEffect}: every id in the entry's target group is destroyed and the
 * rider resolves once afterwards with the number of permanents <em>actually</em> destroyed
 * (indestructible / regenerated ones don't count) on the derived entry's {@code eventValue}, so
 * "for each permanent destroyed this way, …" riders are built from existing effects.
 *
 * <p>Sylvan Primordial = {@code new DestroyTargetPermanentsThenEffect(new SearchLibraryEffect(
 * new EventValue(), new CardSubtypePredicate(CardSubtype.FOREST), LibrarySearchDestination.BATTLEFIELD_TAPPED))}.
 * The rider is skipped entirely when nothing was destroyed, so a zero-count search never shuffles.
 *
 * @param thenEffect          an existing effect resolved after destruction (reused via its own handler)
 * @param cannotBeRegenerated when {@code true} the destruction can't be prevented by regeneration
 */
public record DestroyTargetPermanentsThenEffect(
        CardEffect thenEffect,
        boolean cannotBeRegenerated
) implements RemovalEffect {

    /** Regeneration-allowing destruction with a per-destroyed-count rider. */
    public DestroyTargetPermanentsThenEffect(CardEffect thenEffect) {
        this(thenEffect, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
