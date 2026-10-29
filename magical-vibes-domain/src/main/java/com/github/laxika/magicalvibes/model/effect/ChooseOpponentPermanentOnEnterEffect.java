package com.github.laxika.magicalvibes.model.effect;

/**
 * Mandatory as-enters choice of a permanent the source's controller does not control.
 *
 * @param whenChosenPermanentPutIntoGraveyard when non-null, a delayed trigger registered at the choice that
 *                                            resolves this effect, under the source's controller, once the
 *                                            chosen permanent is put into a graveyard from the battlefield
 */
public record ChooseOpponentPermanentOnEnterEffect(CardEffect whenChosenPermanentPutIntoGraveyard)
        implements CardEffect {

    public ChooseOpponentPermanentOnEnterEffect() {
        this(null);
    }
}
