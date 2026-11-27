package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Internal resolution step: links the cards {@code exiledCardIds} to every token the resolution created from its
 * {@code createdFrom}-th created permanent on, so each of those tokens' "the exiled card" abilities names them:
 * the exile records name the first token, and the others share its link. The Timothar, Baron of Bats ruling:
 * when a replacement creates more than one Bat, they all have the linked ability and any of them returns the card. A
 * handler whose token creation the creating player may stop to order its replacements (CR 616.1) inserts it after
 * itself, so every token the creation made is known. Never put on a card.
 *
 * @param createdFrom   the index in the resolution's created permanents of the first token this creation made
 * @param exiledCardIds the cards the resolution exiled that the tokens are linked to
 */
public record LinkCreatedTokensToExiledCardsEffect(int createdFrom, List<UUID> exiledCardIds) implements CardEffect {

    public LinkCreatedTokensToExiledCardsEffect {
        exiledCardIds = List.copyOf(Objects.requireNonNull(exiledCardIds, "exiledCardIds"));
    }
}
