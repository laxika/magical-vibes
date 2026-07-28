package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles a card matching the given predicate from the controller's hand and imprints it
 * on the source permanent. The description is used in the player prompt. With
 * {@code grantCastPermission} the controller may also cast the exiled card for as long as it
 * remains exiled (Ice Cauldron); the permission never expires on its own.
 * <p>
 * Examples:
 * - Prototype Portal: filter = CardTypePredicate(ARTIFACT), description = "an artifact card"
 * - Semblance Anvil: filter = CardNotPredicate(CardTypePredicate(LAND)), description = "a nonland card"
 * - Ice Cauldron: filter = CardNotPredicate(CardTypePredicate(LAND)), grantCastPermission = true
 */
public record ExileFromHandToImprintEffect(CardPredicate filter, String description,
                                           boolean grantCastPermission) implements CardEffect {

    public ExileFromHandToImprintEffect(CardPredicate filter, String description) {
        this(filter, description, false);
    }
}
