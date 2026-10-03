package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's graveyard, optionally hand, and/or library for a card with the given
 * name and puts it onto the battlefield under their control. Graveyard and hand matches (public /
 * hidden zones the controller already sees) are taken automatically; only the library search
 * presents an interactive pick and shuffles afterwards. Mirrors
 * {@link SearchLibraryAndOrGraveyardForNamedCardToHandEffect} but puts the card onto the
 * battlefield instead of into hand (Gate to the Afterlife's God-Pharaoh's Gift tutor).
 *
 * <p>{@code includeHand} covers wordings that list the hand as a searchable zone (Gate to the
 * Afterlife); Arachnus Spinner searches only graveyard and library. {@code attachToTarget} makes
 * the found card enter attached to the resolving entry's target creature (an Aura tutor); the
 * whole effect does nothing — no search happens — when that target is no longer a legal creature.
 */
public record SearchZonesForCardNamedToBattlefieldEffect(
        String cardName,
        boolean includeHand,
        boolean attachToTarget,
        java.util.List<String> additionalCardNames
) implements CardEffect {

    public SearchZonesForCardNamedToBattlefieldEffect {
        additionalCardNames = java.util.List.copyOf(additionalCardNames);
    }

    public SearchZonesForCardNamedToBattlefieldEffect(String cardName, boolean includeHand, boolean attachToTarget) {
        this(cardName, includeHand, attachToTarget, java.util.List.of());
    }

    public SearchZonesForCardNamedToBattlefieldEffect(java.util.List<String> cardNames) {
        this(cardNames.getFirst(), true, false, java.util.List.copyOf(cardNames.subList(1, cardNames.size())));
    }

    public SearchZonesForCardNamedToBattlefieldEffect(String cardName) {
        this(cardName, true, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return attachToTarget ? TargetSpec.harmful(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
