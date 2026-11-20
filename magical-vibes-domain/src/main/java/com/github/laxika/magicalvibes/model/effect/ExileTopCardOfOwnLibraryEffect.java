package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: "Exile the top card of your library", or "that player / target opponent / that card's owner
 * exiles the top card of their library" with another {@code player}. Each named player exiles from their own library,
 * in APNAP order.
 * Used as part of activated abilities (e.g. Precognition Field's "{3}: Exile the
 * top card of your library.").
 *
 * @param trackWithSource when {@code true}, the exiled card is also tracked in
 *                        {@code permanentExiledCards} under the source permanent
 *                        (e.g. Rona, Disciple of Gix). When {@code false}, the card
 *                        is simply exiled to the player's exile zone.
 * @param player          whose library: {@code you()} by default, {@code targetPlayer()} declares the (harmful)
 *                        player target, {@code triggeringPlayer()} for ingest's "that player",
 *                        {@code ownerOf(triggeringCard())} for "that card's owner"
 */
public record ExileTopCardOfOwnLibraryEffect(boolean trackWithSource, PlayerSubject player) implements CardEffect {

    public ExileTopCardOfOwnLibraryEffect {
        if (player == null) {
            throw new IllegalArgumentException("ExileTopCardOfOwnLibraryEffect requires a player subject");
        }
    }

    public ExileTopCardOfOwnLibraryEffect(boolean trackWithSource) {
        this(trackWithSource, Subjects.you());
    }

    /** "[Player] exiles the top card of their library", untracked. */
    public static ExileTopCardOfOwnLibraryEffect of(PlayerSubject player) {
        return new ExileTopCardOfOwnLibraryEffect(false, player);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
