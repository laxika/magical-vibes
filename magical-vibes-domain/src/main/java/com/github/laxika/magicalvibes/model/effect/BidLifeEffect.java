package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "[Players] bid life. You start the bidding with a bid of [N | any number]. In turn order, each player may top the
 * high bid. The bidding ends if the high bid stands." The controller opens the bidding; then each other bidder, in turn
 * order from the controller (CR 101.4: the next player in turn order), may top the high bid or pass. A player who passes
 * may still top a later high bid; the bidding ends once it comes back around to the high bidder with no raise (Pain's
 * Reward's ruling). A bid is not a payment: a player may bid more life than they have (rulings).
 *
 * <p>A {@link com.github.laxika.magicalvibes.model.StepKind#BID} step: it changes nothing and records, once the high bid
 * stands, the high bid and the high bidder. What the high bidder then does are the steps that follow: "the high bidder
 * loses life equal to the high bid" is {@code LoseLifeEffect(new AmountThisWay(BID), playersThisWay(BID))}, "and gains
 * control of the creature" {@code GainControlOfTargetEffect.gainedBy(playersThisWay(BID))}, "and draws four cards"
 * {@code DrawCardEffect(4, playersThisWay(BID))}, "if you win the bidding" {@code HappenedThisWay.toAPlayer(BID, SELF)}.
 *
 * @param bidders    who may bid ({@code eachPlayer()}, or "you and target spell's controller"); the controller always does
 * @param openingBid the controller's opening bid, or {@code null} for "a bid of any number", which the controller chooses
 * @param prize      what the high bidder wins, for the bid prompt's text and the AI's view of the spell
 */
public record BidLifeEffect(PlayerSubject bidders, Integer openingBid, Prize prize) implements CardEffect {

    /** What the bidding is for, as the bidders are told and the AI reads it. */
    public enum Prize {
        /** "For control of target creature" (Illicit Auction). */
        CONTROL_OF_TARGET,
        /** "If you win the bidding, counter that spell" (Mages' Contest). */
        COUNTER_TARGET_SPELL,
        /** "The high bidder ... draws four cards" (Pain's Reward). */
        CARDS
    }

    public BidLifeEffect {
        Objects.requireNonNull(bidders, "bidders");
        Objects.requireNonNull(prize, "prize");
        if (bidders.declaresTarget()) {
            throw new IllegalArgumentException("The bidders are no target: " + bidders);
        }
        if (openingBid != null && openingBid < 0) {
            throw new IllegalArgumentException("An opening bid is at least 0: " + openingBid);
        }
    }

    /** "Each player may bid life ... You start the bidding with a bid of {@code openingBid}." */
    public static BidLifeEffect eachPlayerStartingAt(int openingBid, Prize prize) {
        return new BidLifeEffect(Subjects.eachPlayer(), openingBid, prize);
    }

    /** "Each player may bid life. You start the bidding with a bid of any number." */
    public static BidLifeEffect eachPlayerStartingAtAnyNumber(Prize prize) {
        return new BidLifeEffect(Subjects.eachPlayer(), null, prize);
    }
}
