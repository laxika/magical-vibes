package com.github.laxika.magicalvibes.model.effect;

/**
 * Internal resolution step of {@link CreateTokensAndAttachEquipmentEffect}: for each token the resolution created from
 * its {@code createdFrom}-th created permanent on, offers its controller to attach an Equipment they control to it
 * ("For each of those tokens, you may attach an Equipment you control to it"). The creating handler inserts it after
 * itself, because the creating player may stop the creation to order its replacements (CR 616.1): every token the
 * creation made is known only once it has finished. Never put on a card.
 *
 * @param createdFrom the index in the resolution's created permanents of the first token the creation made
 */
public record AttachEquipmentToCreatedTokensEffect(int createdFrom) implements CardEffect {
}
