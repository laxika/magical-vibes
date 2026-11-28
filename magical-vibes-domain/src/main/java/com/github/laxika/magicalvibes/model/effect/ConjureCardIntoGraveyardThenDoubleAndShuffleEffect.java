package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;

import java.util.Objects;

/**
 * Conjures a card of {@code printing} into the controller's graveyard, perpetually doubles both cards' P/T, then shuffles them. The
 * other card is the one that died, the stack entry's triggering card.
 */
public record ConjureCardIntoGraveyardThenDoubleAndShuffleEffect(SpellbookCard printing)
        implements CardEffect {

    public ConjureCardIntoGraveyardThenDoubleAndShuffleEffect {
        Objects.requireNonNull(printing, "printing");
    }
}
