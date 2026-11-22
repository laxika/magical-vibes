package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/**
 * "Target player reveals their hand", "reveals a card at random from their hand", "reveal a card in your hand", "look
 * at a card at random in target player's hand": cards of the hand of each player {@link #player()} names are shown.
 * A reveal shows them to every player (CR 701.20a); a look only to the players {@link #seenBy()} names
 * (CR 701.20e), its log line naming no card. The cards stay in the hand and the step records them
 * ({@code StepKind.REVEAL}): "that card" is {@code Subjects.revealedThisWay()}, "that card's mana value"
 * {@code AmountThisWay.revealedManaValue()}.
 *
 * @param player   whose hand; only {@code targetPlayer()} declares a (benign) player target
 * @param cards    which cards of it ("the creature cards in", a card an earlier step chose); {@code null} for all
 * @param atRandom how many of them, chosen at random; {@code null} for every one
 * @param seenBy   who looks; {@code null} for a reveal, which everybody sees
 */
public record RevealCardsFromHandEffect(PlayerSubject player, CardSubject cards, DynamicAmount atRandom,
                                        PlayerSubject seenBy) implements CardEffect {

    public RevealCardsFromHandEffect {
        Objects.requireNonNull(player, "player");
        if (cards != null && cards.declaresTarget() && !player.declaresTarget()) {
            throw new IllegalArgumentException("The player whose hand is shown declares the target: " + cards);
        }
        if (seenBy != null && seenBy.declaresTarget()) {
            throw new IllegalArgumentException("Who looks is no target of this step: " + seenBy);
        }
    }

    /** "[Player] reveals their hand." */
    public static RevealCardsFromHandEffect hand(PlayerSubject player) {
        return new RevealCardsFromHandEffect(player, null, null, null);
    }

    /** "[Player] reveals [count] card(s) at random from their hand." */
    public static RevealCardsFromHandEffect atRandom(PlayerSubject player, int count) {
        return new RevealCardsFromHandEffect(player, null, new Fixed(count), null);
    }

    /** "[Player] reveals [those cards] from their hand." */
    public static RevealCardsFromHandEffect cards(PlayerSubject player, CardSubject cards) {
        return new RevealCardsFromHandEffect(player, Objects.requireNonNull(cards, "cards"), null, null);
    }

    /** "You look at ...": shown to the players {@code viewer} names alone. */
    public RevealCardsFromHandEffect seenOnlyBy(PlayerSubject viewer) {
        return new RevealCardsFromHandEffect(player, cards, atRandom, Objects.requireNonNull(viewer, "viewer"));
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
