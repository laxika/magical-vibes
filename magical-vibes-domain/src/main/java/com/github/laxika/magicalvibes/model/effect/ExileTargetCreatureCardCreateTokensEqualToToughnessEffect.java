package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile target creature card from a graveyard, then create X copies of {@code tokenTemplate},
 * where X is the exiled card's toughness."
 *
 * <p>At resolution the single graveyard target (any graveyard) is exiled and its printed toughness
 * is captured; that many copies of {@code tokenTemplate} are then created for the controller. The
 * {@code amount} on {@code tokenTemplate} is ignored — the count is always the exiled card's
 * toughness. If the sole target has left the graveyard the whole spell fizzles before this effect
 * runs (CR 608.2b), so no tokens are created.
 *
 * <p>The graveyard-exile analog of {@link SacrificeCreatureToCreateTokensEqualToToughnessEffect}.
 * Used by Morbid Bloom (1/1 green Saproling template).
 *
 * @param tokenTemplate the token to create (power/toughness/color/subtypes/etc.); its amount is ignored
 */
public record ExileTargetCreatureCardCreateTokensEqualToToughnessEffect(
        CreateTokenEffect tokenTemplate) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.ANY_GRAVEYARD_CARD);
    }
}
