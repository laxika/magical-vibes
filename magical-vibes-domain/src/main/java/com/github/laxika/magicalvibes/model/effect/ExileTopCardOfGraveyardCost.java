package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Cost effect that requires exiling the top card of the controller's graveyard — the card most
 * recently put there, i.e. the last element of the append-ordered graveyard list. There is no
 * choice involved; no matching card makes the cost unpayable.
 *
 * <p>{@code requiredType} narrows it to "the top [type] card of your graveyard": the matching card
 * closest to the top, with nonmatching cards above it skipped rather than blocking. {@code null}
 * means any card.</p>
 *
 * <p>Used by Alms ({@code {1}}, Exile the top card of your graveyard) as an activation cost and by
 * Barrow Ghoul ({@code CREATURE}) as the payable side of a {@code ForcedCostOrElseEffect}.</p>
 */
public record ExileTopCardOfGraveyardCost(CardType requiredType) implements CostEffect {

    /** "Exile the top card of your graveyard" — no type restriction. */
    public ExileTopCardOfGraveyardCost() {
        this(null);
    }
}
