package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * "Exile a card at random from your graveyard", "exile a random card from your library", "exile a random creature card
 * from that player's library": each player {@link #player()} names (in APNAP order) exiles one card chosen at random
 * among the cards of their {@link #zone()} that match {@link #filter()}, and nothing when none matches. The step that
 * follows reads the card as {@code Subjects.cardsThisWay(StepKind.EXILE)}: "you may play that card this turn" is
 * {@link AllowPlayCardsFromExileEffect}, "copy it. You may cast the copy" is
 * {@code CopyImprintedCardAndMayCastCopyEffect.of(...)}, "create a token that's a copy of that card" is
 * {@link CreateTokenCopyOfCardEffect#of}.
 *
 * <p>The filter may compare with the triggering event's value ("a random card with mana value equal to the damage
 * dealt": {@code CardManaValueEqualsEventValuePredicate}).
 *
 * <p>{@code count} is "exile three cards at random": each player exiles that many different cards, or as many as
 * match (CR 609.3), as one step (Capricious Hellraiser).
 *
 * @param count  how many cards each player exiles
 * @param zone   where the card is exiled from: {@link Zone#GRAVEYARD} or {@link Zone#LIBRARY}
 * @param player whose zone: {@code you()}, or a player read without a target ({@code triggeringPlayer()} for "that
 *               player's library")
 * <p>{@code linkedToSource} ({@link #withSource()}) is "exile ... with this enchantment" (CR 607.2a): the cards are
 * exiled with the source permanent, so a linked ability reads them as {@code Subjects.exiledWithSource()} (Arcane
 * Bombardment).
 *
 * @param filter     which cards may be picked; {@code null} for any card
 * @param linkedToSource whether the cards are exiled with the source permanent
 */
public record ExileCardAtRandomEffect(Zone zone, PlayerSubject player, CardPredicate filter, int count,
                                      boolean linkedToSource)
        implements CardEffect {

    public ExileCardAtRandomEffect {
        Objects.requireNonNull(zone, "zone");
        Objects.requireNonNull(player, "player");
        if (zone != Zone.GRAVEYARD && zone != Zone.LIBRARY && zone != Zone.HAND) {
            throw new IllegalArgumentException("A random card is exiled from a graveyard, a library or a hand, not "
                    + zone);
        }
        if (count < 1) {
            throw new IllegalArgumentException("At least one card is exiled: " + count);
        }
        if (player.declaresTarget()) {
            throw new IllegalArgumentException("The random exile names its player without a target: " + player);
        }
    }

    /** One card. */
    public ExileCardAtRandomEffect(Zone zone, PlayerSubject player, CardPredicate filter) {
        this(zone, player, filter, 1, false);
    }

    /** This exile of {@code cards} cards at random. */
    public ExileCardAtRandomEffect cards(int cards) {
        return new ExileCardAtRandomEffect(zone, player, filter, cards, linkedToSource);
    }

    /** This exile, the cards exiled with the source permanent ("the cards exiled with this", CR 607.2a). */
    public ExileCardAtRandomEffect withSource() {
        return new ExileCardAtRandomEffect(zone, player, filter, count, true);
    }

    /** "Exile a [filter] card at random from [player]'s graveyard"; {@code filter} {@code null} for any card. */
    public static ExileCardAtRandomEffect fromGraveyard(PlayerSubject player, CardPredicate filter) {
        return new ExileCardAtRandomEffect(Zone.GRAVEYARD, player, filter);
    }

    /** "Exile a random [filter] card from [player]'s library"; {@code filter} {@code null} for any card. */
    public static ExileCardAtRandomEffect fromLibrary(PlayerSubject player, CardPredicate filter) {
        return new ExileCardAtRandomEffect(Zone.LIBRARY, player, filter);
    }

    /** "Exiles a card at random from their hand." */
    public static ExileCardAtRandomEffect fromHand(PlayerSubject player) {
        return new ExileCardAtRandomEffect(Zone.HAND, player, null);
    }
}
