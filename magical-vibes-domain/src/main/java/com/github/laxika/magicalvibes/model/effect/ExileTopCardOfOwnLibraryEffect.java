package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * One-shot effect: "Exile the top card of your library", or "that player / target opponent / that card's owner
 * exiles the top card of their library" with another {@code player}, or "each opponent exiles the top two cards of
 * their library" with a {@code count}. Each named player exiles from their own library, in APNAP order. No player may
 * play the cards (a permission is {@link ExileTopCardMayPlayThisTurnEffect}).
 * Used as part of activated abilities (e.g. Precognition Field's "{3}: Exile the
 * top card of your library.").
 *
 * @param trackWithSource when {@code true}, the exiled card is also tracked in
 *                        {@code permanentExiledCards} under the source permanent
 *                        (e.g. Rona, Disciple of Gix). When {@code false}, the card
 *                        is simply exiled to the player's exile zone. Only for one card.
 * @param player          whose library: {@code you()} by default, {@code targetPlayer()} declares the (harmful)
 *                        player target, {@code triggeringPlayer()} for ingest's "that player",
 *                        {@code ownerOf(triggeringCard())} for "that card's owner"
 * @param count           how many cards each named player exiles, evaluated once as the effect resolves (with the
 *                        source as it is or last was); a library with fewer exiles what it has
 * @param linkedToSource  "exiled with this creature" without an imprint: each card is exiled with the source permanent
 *                        (the entry's, even after it left), which "when you play a card exiled with this creature"
 *                        and "you may play cards exiled with this enchantment" read (Voltaic Visionary, Pick Up the
 *                        Pace); {@code trackWithSource} links and also imprints
 */
public record ExileTopCardOfOwnLibraryEffect(boolean trackWithSource, PlayerSubject player, DynamicAmount count,
                                             boolean linkedToSource)
        implements CardEffect {

    public ExileTopCardOfOwnLibraryEffect {
        if (player == null) {
            throw new IllegalArgumentException("ExileTopCardOfOwnLibraryEffect requires a player subject");
        }
        if (count == null) {
            throw new IllegalArgumentException("ExileTopCardOfOwnLibraryEffect requires a count");
        }
        if (trackWithSource && !(count instanceof Fixed fixed && fixed.value() == 1)) {
            throw new IllegalArgumentException("ExileTopCardOfOwnLibraryEffect tracks only one card with its source");
        }
        if (trackWithSource && linkedToSource) {
            throw new IllegalArgumentException("trackWithSource already links the card to the source");
        }
    }

    public ExileTopCardOfOwnLibraryEffect(boolean trackWithSource, PlayerSubject player, DynamicAmount count) {
        this(trackWithSource, player, count, false);
    }

    public ExileTopCardOfOwnLibraryEffect(boolean trackWithSource, PlayerSubject player) {
        this(trackWithSource, player, new Fixed(1));
    }

    public ExileTopCardOfOwnLibraryEffect(boolean trackWithSource) {
        this(trackWithSource, Subjects.you());
    }

    /** "[Player] exiles the top card of their library", untracked. */
    public static ExileTopCardOfOwnLibraryEffect of(PlayerSubject player) {
        return new ExileTopCardOfOwnLibraryEffect(false, player);
    }

    /** "[Player] exiles the top {@code count} cards of their library", untracked. */
    public static ExileTopCardOfOwnLibraryEffect of(PlayerSubject player, int count) {
        return of(player, new Fixed(count));
    }

    /** "Exile the top card of your library", exiled with the source permanent ({@link #linkedToSource}). */
    public static ExileTopCardOfOwnLibraryEffect linkedToSource(PlayerSubject player) {
        return new ExileTopCardOfOwnLibraryEffect(false, player, new Fixed(1), true);
    }

    /** "[Player] exiles the top X cards of their library", untracked. */
    public static ExileTopCardOfOwnLibraryEffect of(PlayerSubject player, DynamicAmount count) {
        return new ExileTopCardOfOwnLibraryEffect(false, player, count);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
