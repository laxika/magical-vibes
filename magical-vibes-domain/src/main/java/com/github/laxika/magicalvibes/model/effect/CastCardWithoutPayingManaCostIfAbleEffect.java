package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "[The player] casts it without paying its mana cost if able": the mandatory cast-now step. The card {@link #card}
 * names, in exile or in a hand, is cast by the player whose zone it is in, as part of the resolution (CR 608.2g: the
 * casting steps, then the resolution goes on), with no "may" asked. Casting it without paying its mana cost is its
 * alternative cost (CR 118.9), so its cost increases and mandatory additional costs are still paid (CR 601.2f); a
 * card that can't be cast that way (a land card, nothing it can target, a "can't" that stops the cast, CR 601.3, a
 * cost that can't be paid) stays where it is.
 *
 * <p>"If it's a land card, the player puts it onto the battlefield. Otherwise, ..." is
 * {@code PutCardsOntoBattlefieldEffect(cardsThisWay(kind, land))} before this step (Omen Machine, Wild Evocation).
 *
 * @param card the card to cast: one card, a step result such as {@code cardsThisWay(EXILE)} or
 *             {@code revealedThisWay()}
 */
public record CastCardWithoutPayingManaCostIfAbleEffect(CardSubject card) implements CardEffect {

    public CastCardWithoutPayingManaCostIfAbleEffect {
        Objects.requireNonNull(card, "card");
    }

    @Override
    public TargetSpec targetSpec() {
        return card.targetSpec();
    }
}
