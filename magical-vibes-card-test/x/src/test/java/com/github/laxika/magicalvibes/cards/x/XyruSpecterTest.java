package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XyruSpecter.class, DarkRitual.class, Forest.class, GrizzlyBears.class})
class XyruSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged opponent chooses to discard or challenge")
    void damagedOpponentChoosesMode() {
        addAttackingSpecter();
        harness.setHand(player2, List.of(new Forest()));

        resolveCombatAndTrigger();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly("Discard a card", "Challenge");
    }

    @Test
    @DisplayName("A challenge showing two black cards makes the opponent discard two cards")
    void successfulChallengeDiscardsTwo() {
        addAttackingSpecter();
        DarkRitual firstBlackCard = new DarkRitual();
        DarkRitual secondBlackCard = new DarkRitual();
        harness.setHand(player1, List.of(firstBlackCard, secondBlackCard));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, "Challenge");

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice revealChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(revealChoice.playerId()).isEqualTo(player1.getId());
        assertThat(revealChoice.validCardIds()).containsExactly(firstBlackCard.getId(), secondBlackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBlackCard.getId(), secondBlackCard.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A challenge without two revealed black cards does nothing")
    void unsuccessfulChallengeDoesNotDiscard() {
        addAttackingSpecter();
        DarkRitual blackCard = new DarkRitual();
        harness.setHand(player1, List.of(blackCard, new Forest()));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, "Challenge");
        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the challenge makes the damaged opponent discard exactly one card")
    void decliningChallengeDiscardsOne() {
        addAttackingSpecter();
        DarkRitual controllerCard = new DarkRitual();
        Forest discardedCard = new Forest();
        GrizzlyBears keptCard = new GrizzlyBears();
        harness.setHand(player1, List.of(controllerCard));
        harness.setHand(player2, List.of(discardedCard, keptCard));

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, "Discard a card");
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may decline to reveal even when two black cards are available")
    void revealingNoCardsDoesNotDiscard() {
        addAttackingSpecter();
        DarkRitual firstBlackCard = new DarkRitual();
        DarkRitual secondBlackCard = new DarkRitual();
        harness.setHand(player1, List.of(firstBlackCard, secondBlackCard));
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, "Challenge");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstBlackCard, secondBlackCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A challenge against an empty hand does not make the opponent discard")
    void challengingEmptyHandDoesNotDiscard() {
        addAttackingSpecter();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, "Challenge");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A challenge permits revealing nonblack cards, but one black card is insufficient")
    void mixedRevealWithOneBlackCardDoesNotDiscard() {
        addAttackingSpecter();
        DarkRitual blackCard = new DarkRitual();
        Forest nonblackCard = new Forest();
        harness.setHand(player1, List.of(blackCard, nonblackCard));
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, "Challenge");

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice.validCardIds()).containsExactly(blackCard.getId(), nonblackCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId(), nonblackCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blackCard, nonblackCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Revealing two black cards alongside a nonblack card still wins the challenge")
    void mixedRevealWithTwoBlackCardsDiscardsTwo() {
        addAttackingSpecter();
        DarkRitual firstBlackCard = new DarkRitual();
        DarkRitual secondBlackCard = new DarkRitual();
        Forest nonblackCard = new Forest();
        harness.setHand(player1, List.of(firstBlackCard, secondBlackCard, nonblackCard));
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears(), new Forest()));

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, "Challenge");
        harness.handleMultipleCardsChosen(player1,
                List.of(firstBlackCard.getId(), secondBlackCard.getId(), nonblackCard.getId()));
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstBlackCard, secondBlackCard, nonblackCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A successful challenge discards the opponent's only card when fewer than two remain")
    void successfulChallengeAgainstOneCardDiscardsAvailableCard() {
        addAttackingSpecter();
        DarkRitual firstBlackCard = new DarkRitual();
        DarkRitual secondBlackCard = new DarkRitual();
        Forest discardedCard = new Forest();
        harness.setHand(player1, List.of(firstBlackCard, secondBlackCard));
        harness.setHand(player2, List.of(discardedCard));

        resolveCombatAndTrigger();
        harness.handleListChoice(player2, "Challenge");
        harness.handleMultipleCardsChosen(player1, List.of(firstBlackCard.getId(), secondBlackCard.getId()));
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addAttackingSpecter() {
        Permanent specter = addCreatureReady(player1, new XyruSpecter());
        specter.setAttacking(true);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
