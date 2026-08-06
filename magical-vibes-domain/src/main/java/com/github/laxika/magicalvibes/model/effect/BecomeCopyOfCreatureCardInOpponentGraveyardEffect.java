package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Triggered effect: the source permanent becomes a copy of a creature card that was just put into an
 * opponent's graveyard, except its name stays the source's printed name, it is legendary in addition
 * to its other types, and it has hexproof and the triggered ability that granted this effect. Used by
 * Lazav, Dimir Mastermind.
 *
 * <p>The ability does not target and does not require the card to still be in the graveyard when it
 * resolves, so the triggering card is captured at trigger time and copied as last-known information.
 *
 * @param graveyardCard the creature card that was put into the opponent's graveyard ({@code null} in
 *                      the card definition, filled in at trigger time)
 */
public record BecomeCopyOfCreatureCardInOpponentGraveyardEffect(Card graveyardCard) implements CardEffect {

    public BecomeCopyOfCreatureCardInOpponentGraveyardEffect() {
        this(null);
    }
}
