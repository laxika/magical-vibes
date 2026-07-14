package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Capability interface for effects that create tokens of one profile. Lets consumers — chiefly the
 * AI evaluators — ask "how many tokens of what profile" without knowing the concrete effect type,
 * mirroring how {@link ManaProducingEffect} abstracts mana production.
 *
 * <p>Descriptive only: it states facts drawn from the record's existing components, never a score.
 */
public interface TokenCreatingEffect extends CardEffect {

    /** The number of tokens created, as a {@link DynamicAmount} evaluated at resolution. */
    DynamicAmount tokenAmount();

    /** The token's primary card type (e.g. CREATURE, ARTIFACT). */
    CardType tokenType();

    /** The token's printed power. */
    int tokenPower();

    /** The token's printed toughness. */
    int tokenToughness();
}
