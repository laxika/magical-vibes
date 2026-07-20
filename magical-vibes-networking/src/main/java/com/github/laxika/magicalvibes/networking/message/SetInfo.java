package com.github.laxika.magicalvibes.networking.message;

/**
 * A card set offered in the lobby. {@code randomEligible} marks sets complete enough to be a source
 * for set-restricted "All Random" decks; the card browser, deck builder and draft ignore it.
 */
public record SetInfo(String code, String name, boolean randomEligible) {
}
