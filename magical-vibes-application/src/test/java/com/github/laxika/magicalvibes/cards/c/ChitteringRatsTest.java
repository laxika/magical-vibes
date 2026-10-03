package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelBrute;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.e.EchoingTruth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChitteringRats.class, DarksteelBrute.class, DarksteelCitadel.class, EchoingTruth.class})
class ChitteringRatsTest extends BaseCardTest {

    @Test
    @DisplayName("When Chittering Rats enters, its ETB asks an opponent to choose a card for their library")
    void putsChosenCardOnTopOfOpponentsLibrary() {
        Card chosenCard = new DarksteelBrute();
        Card remainingCard = new EchoingTruth();
        Card oldTop = new DarksteelCitadel();
        harness.setHand(player1, List.of(new ChitteringRats()));
        harness.setHand(player2, new ArrayList<>(List.of(chosenCard, remainingCard)));
        harness.setLibrary(player2, List.of(oldTop));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PermanentChoice.class, choice -> {
                    assertThat(choice.playerId()).isEqualTo(player1.getId());
                    assertThat(choice.validIds()).containsExactly(player2.getId());
                });
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.playerId()).isEqualTo(player2.getId());
                    assertThat(choice.minCount()).isEqualTo(1);
                    assertThat(choice.maxCount()).isEqualTo(1);
                });

        harness.handleMultipleCardsChosen(player2, List.of(chosenCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(chosenCard, oldTop);
        harness.assertOnBattlefield(player1, "Chittering Rats");
    }

    @Test
    @DisplayName("An opponent with an empty hand does not get a card choice")
    void emptyHandDoesNothing() {
        harness.setHand(player1, List.of(new ChitteringRats()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PermanentChoice.class, choice ->
                        assertThat(choice.validIds()).containsExactly(player2.getId()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Chittering Rats");
    }

    @Test
    @DisplayName("Chittering Rats cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ChitteringRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PermanentChoice.class, choice ->
                        assertThat(choice.validIds()).containsExactly(player2.getId()));
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The opponent must choose exactly one of their own cards, even with an empty library")
    void mandatoryChoiceWithEmptyLibrary() {
        Card chosenCard = new DarksteelBrute();
        Card remainingCard = new DarksteelCitadel();
        Card controllersCard = new EchoingTruth();
        harness.setHand(player1, List.of(new ChitteringRats(), controllersCard));
        harness.setHand(player2, List.of(chosenCard, remainingCard));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2,
                List.of(chosenCard.getId(), remainingCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(controllersCard.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player2, List.of(chosenCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(chosenCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllersCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The enter trigger resolves even if Chittering Rats returns to hand in response")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Card chosenCard = new DarksteelBrute();
        Card oldTop = new DarksteelCitadel();
        harness.setHand(player1, List.of(new ChitteringRats(), new EchoingTruth()));
        harness.setHand(player2, List.of(chosenCard));
        harness.setLibrary(player2, List.of(oldTop));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Chittering Rats"));

        harness.assertNotOnBattlefield(player1, "Chittering Rats");
        harness.assertInHand(player1, "Chittering Rats");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(chosenCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(chosenCard, oldTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
