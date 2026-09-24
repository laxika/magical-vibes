package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/**
 * "Exile target creature card from a graveyard, then create X copies of {@code tokenTemplate},
 * where X is the exiled card's toughness."
 *
 * <p>At resolution the single graveyard target (in {@code graveyardScope}) is exiled and its
 * printed toughness is captured; that many copies of {@code tokenTemplate} are then created for
 * the controller. The {@code amount} on {@code tokenTemplate} is ignored — the count is always
 * the exiled card's
 * toughness. If the sole target has left the graveyard the whole spell fizzles before this effect
 * runs (CR 608.2b), so no tokens are created.
 *
 * <p>The graveyard-exile analog of {@link SacrificeCreatureToCreateTokensEqualToToughnessEffect}.
 * Used by Morbid Bloom (1/1 green Saproling template).
 *
 * @param tokenTemplate the token to create (power/toughness/color/subtypes/etc.); its amount is ignored
 * @param graveyardScope which graveyard(s) may contain the target
 */
public record ExileTargetCreatureCardCreateTokensEqualToToughnessEffect(
        CreateTokenEffect tokenTemplate,
        GraveyardSearchScope graveyardScope) implements CardEffect {

    public ExileTargetCreatureCardCreateTokensEqualToToughnessEffect(CreateTokenEffect tokenTemplate) {
        this(tokenTemplate, GraveyardSearchScope.ALL_GRAVEYARDS);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(graveyardScope));
    }
}
