package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles a card matching the given predicate from the controller's hand and imprints it
 * on the source permanent. The description is used in the player prompt. With
 * {@code faceDown}, the card is exiled face down. With
 * {@code grantCastPermission} the controller may also cast the exiled card for as long as it
 * remains exiled (Ice Cauldron); the permission never expires on its own.
 * <p>
 * Examples:
 * - Prototype Portal: filter = CardTypePredicate(ARTIFACT), description = "an artifact card"
 * - Semblance Anvil: filter = CardNotPredicate(CardTypePredicate(LAND)), description = "a nonland card"
 * - Ice Cauldron: filter = CardNotPredicate(CardTypePredicate(LAND)), grantCastPermission = true
 */
public record ExileFromHandToImprintEffect(CardPredicate filter, String description,
                                           boolean grantCastPermission,
                                           boolean manaValueEqualsX,
                                           boolean faceDown) implements CardEffect {

    public ExileFromHandToImprintEffect(CardPredicate filter, String description) {
        this(filter, description, false, false, false);
    }

    public ExileFromHandToImprintEffect(CardPredicate filter, String description,
                                        boolean grantCastPermission) {
        this(filter, description, grantCastPermission, false, false);
    }

    /** Exiles a matching card whose mana value equals the activated ability's chosen X. */
    public static ExileFromHandToImprintEffect withManaValueX(CardPredicate filter, String description) {
        return new ExileFromHandToImprintEffect(filter, description, false, true, false);
    }

    /** Exiles a matching card face down and imprints it on the source permanent. */
    public static ExileFromHandToImprintEffect faceDown(CardPredicate filter, String description) {
        return new ExileFromHandToImprintEffect(filter, description, false, false, true);
    }
}
