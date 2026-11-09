package com.github.laxika.magicalvibes.model.effect;

/**
 * Conjures a duplicate of each card a seek event was about into its controller's hand, then manifests those duplicates:
 * "conjure a duplicate of each of those cards into your hand, then manifest those duplicates" (Lurker in the Deep).
 * "Those cards" are the stack entry's triggering cards ({@code StackEntry.getTriggeringCards()}), which the seek
 * collector records as the ability triggers.
 */
public record ConjureDuplicatesOfSoughtCardsAndManifestEffect() implements CardEffect {
}
