package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Internal resolution step: attaches the Aura tokens the resolution created, from its {@code createdFrom}-th created
 * permanent on, to {@code hostIds}, split over them in order: one each when the creation made as many as there are
 * hosts, two each after a doubler (the Asinine Antics ruling: Roles Doubling Season doubles become attached to that
 * permanent at the same time). A handler whose token creation the creating player may stop to order its replacements
 * (CR 616.1) inserts it after itself, so the tokens are attached once the creation has finished and every token it
 * made, however the replacements changed the count, is known. Tokens of other kinds the creation made (a Squirrel
 * Chatterfang added, a Food Tippy-Toe added) are left alone. Never put on a card.
 *
 * @param createdFrom the index in the resolution's created permanents of the first token this creation made
 * @param hostIds     the permanents the tokens are created attached to, one per token before replacements
 */
public record AttachCreatedTokensToHostsEffect(int createdFrom, List<UUID> hostIds) implements CardEffect {

    public AttachCreatedTokensToHostsEffect {
        hostIds = List.copyOf(Objects.requireNonNull(hostIds, "hostIds"));
    }
}
