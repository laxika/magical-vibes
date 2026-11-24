package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Lets each player, or each player other than the effect's controller, optionally put matching
 * card(s) from their hand onto the battlefield. Choices are made in active-player order and the
 * chosen cards normally enter simultaneously. A non-repeating controller-first effect puts each
 * chosen card onto the battlefield immediately. The repeating mode also puts each chosen card
 * immediately and starts another round beginning with the effect's controller after any card
 * enters. With {@code players}, only the players it names choose, in APNAP order ("each player who received no votes may
 * put a permanent card from their hand onto the battlefield").
 */
public record EachPlayerMayPutCardFromHandToBattlefieldEffect(CardPredicate predicate, String label,
                                                              boolean opponentsOnly,
                                                              boolean repeatUntilNoOne,
                                                              boolean startsWithController,
                                                              boolean anyNumber,
                                                              PlayerSubject players)
        implements CardEffect {

    public EachPlayerMayPutCardFromHandToBattlefieldEffect {
        if (players != null && (opponentsOnly || repeatUntilNoOne || startsWithController)) {
            throw new IllegalArgumentException("The players who choose are named once: " + players);
        }
        if (players != null && players.declaresTarget()) {
            throw new IllegalArgumentException("The players who choose are not targeted: " + players);
        }
    }

    public EachPlayerMayPutCardFromHandToBattlefieldEffect(CardPredicate predicate, String label,
                                                            boolean opponentsOnly, boolean repeatUntilNoOne,
                                                            boolean startsWithController, boolean anyNumber) {
        this(predicate, label, opponentsOnly, repeatUntilNoOne, startsWithController, anyNumber, null);
    }

    public EachPlayerMayPutCardFromHandToBattlefieldEffect(CardPredicate predicate, String label) {
        this(predicate, label, false, false, false, false);
    }

    public EachPlayerMayPutCardFromHandToBattlefieldEffect(CardPredicate predicate, String label,
                                                            boolean opponentsOnly) {
        this(predicate, label, opponentsOnly, false, false, false);
    }

    public EachPlayerMayPutCardFromHandToBattlefieldEffect(CardPredicate predicate, String label,
                                                            boolean opponentsOnly,
                                                            boolean repeatUntilNoOne,
                                                            boolean startsWithController) {
        this(predicate, label, opponentsOnly, repeatUntilNoOne, startsWithController, false);
    }

    /** Show and Tell's artifact, creature, enchantment, or land choice. */
    public static EachPlayerMayPutCardFromHandToBattlefieldEffect showAndTell() {
        return new EachPlayerMayPutCardFromHandToBattlefieldEffect(permanentCardPredicate(),
                "artifact, creature, enchantment, or land");
    }

    /** Hypergenesis's repeating, sequential artifact, creature, enchantment, or land choice. */
    public static EachPlayerMayPutCardFromHandToBattlefieldEffect hypergenesis() {
        return new EachPlayerMayPutCardFromHandToBattlefieldEffect(permanentCardPredicate(),
                "artifact, creature, enchantment, or land", false, true, true);
    }

    public static EachPlayerMayPutCardFromHandToBattlefieldEffect eachOpponent(CardPredicate predicate,
                                                                                 String label) {
        return new EachPlayerMayPutCardFromHandToBattlefieldEffect(predicate, label, true);
    }

    /** Tempting Wurm's optional multi-card permanent entry for each opponent. */
    public static EachPlayerMayPutCardFromHandToBattlefieldEffect temptingWurm() {
        return new EachPlayerMayPutCardFromHandToBattlefieldEffect(permanentCardPredicate(),
                "artifact, creature, enchantment, or land", true, false, false, true);
    }

    /** "Each [player {@code players} names] may put a [card matching {@code predicate}] from their hand onto the battlefield". */
    public static EachPlayerMayPutCardFromHandToBattlefieldEffect by(PlayerSubject players, CardPredicate predicate,
                                                                     String label) {
        return new EachPlayerMayPutCardFromHandToBattlefieldEffect(predicate, label, false, false, false, false,
                java.util.Objects.requireNonNull(players, "players"));
    }

    private static CardPredicate permanentCardPredicate() {
        return new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.ENCHANTMENT),
                new CardTypePredicate(CardType.LAND)));
    }
}
