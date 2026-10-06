package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SadisticAugermage.class, LastGasp.class, GrayscaledGharial.class, ScreechingGriffin.class})
class SadisticAugermageTest extends BaseCardTest {

    @Test
    @DisplayName("When Sadistic Augermage dies, each player puts a card from hand on top of their library")
    void eachPlayerPutsAHandCardOnTop() {
        Permanent augermage = harness.addToBattlefieldAndReturn(player1, new SadisticAugermage());
        Card player1Card = new GrayscaledGharial();
        Card player1UnchosenCard = new ScreechingGriffin();
        Card player2Card = new ScreechingGriffin();
        Card player1OldTop = new ScreechingGriffin();
        Card player2OldTop = new GrayscaledGharial();
        Card lastGasp = new LastGasp();
        harness.setHand(player1, List.of(lastGasp, player1Card, player1UnchosenCard));
        harness.setHand(player2, List.of(player2Card));
        harness.setLibrary(player1, List.of(player1OldTop));
        harness.setLibrary(player2, List.of(player2OldTop));

        kill(augermage);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice ->
                        assertThat(choice.playerId()).isEqualTo(player1.getId()));

        harness.handleMultipleCardsChosen(player1, List.of(player1Card.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice ->
                        assertThat(choice.playerId()).isEqualTo(player2.getId()));

        harness.handleMultipleCardsChosen(player2, List.of(player2Card.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1UnchosenCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).startsWith(player1Card, player1OldTop);
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(player2Card, player2OldTop);
    }

    @Test
    @DisplayName("No player is prompted when every hand is empty")
    void emptyHandsDoNotPrompt() {
        Permanent augermage = harness.addToBattlefieldAndReturn(player1, new SadisticAugermage());
        Card lastGasp = new LastGasp();
        harness.setHand(player1, List.of(lastGasp));
        harness.setHand(player2, List.of());

        kill(augermage);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Skips an empty hand and prompts the other player")
    void skipsEmptyHandAndPromptsOtherPlayer() {
        Permanent augermage = harness.addToBattlefieldAndReturn(player1, new SadisticAugermage());
        Card lastGasp = new LastGasp();
        Card player2Card = new GrayscaledGharial();
        Card player2OldTop = new ScreechingGriffin();
        harness.setHand(player1, List.of(lastGasp));
        harness.setHand(player2, List.of(player2Card));
        harness.setLibrary(player2, List.of(player2OldTop));

        kill(augermage);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice ->
                        assertThat(choice.playerId()).isEqualTo(player2.getId()));

        harness.handleMultipleCardsChosen(player2, List.of(player2Card.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(player2Card, player2OldTop);
    }

    @Test
    @DisplayName("The active player chooses first even when they do not control Sadistic Augermage")
    void startsWithActivePlayer() {
        Permanent augermage = harness.addToBattlefieldAndReturn(player1, new SadisticAugermage());
        Card lastGasp = new LastGasp();
        Card player1Card = new GrayscaledGharial();
        Card player2Card = new ScreechingGriffin();
        Card player1OldTop = new ScreechingGriffin();
        Card player2OldTop = new GrayscaledGharial();
        harness.setHand(player1, List.of(lastGasp, player1Card));
        harness.setHand(player2, List.of(player2Card));
        harness.setLibrary(player1, List.of(player1OldTop));
        harness.setLibrary(player2, List.of(player2OldTop));

        kill(augermage, player2);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice ->
                        assertThat(choice.playerId()).isEqualTo(player2.getId()));

        harness.handleMultipleCardsChosen(player2, List.of(player2Card.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice ->
                        assertThat(choice.playerId()).isEqualTo(player1.getId()));

        harness.handleMultipleCardsChosen(player1, List.of(player1Card.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).startsWith(player1Card, player1OldTop);
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(player2Card, player2OldTop);
    }

    @Test
    @DisplayName("All players choose before any chosen card moves to a library")
    void chosenCardsMoveSimultaneously() {
        Permanent augermage = harness.addToBattlefieldAndReturn(player1, new SadisticAugermage());
        Card player1Card = new GrayscaledGharial();
        Card player2Card = new ScreechingGriffin();
        Card player1OldTop = new ScreechingGriffin();
        Card player2OldTop = new GrayscaledGharial();
        harness.setHand(player1, List.of(new LastGasp(), player1Card));
        harness.setHand(player2, List.of(player2Card));
        harness.setLibrary(player1, List.of(player1OldTop));
        harness.setLibrary(player2, List.of(player2OldTop));

        kill(augermage);
        harness.handleMultipleCardsChosen(player1, List.of(player1Card.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Card);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1OldTop);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Card);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2OldTop);

        harness.handleMultipleCardsChosen(player2, List.of(player2Card.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Card, player1OldTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Card, player2OldTop);
    }

    @Test
    @DisplayName("Putting a card on an empty library is mandatory when a hand card is available")
    void mandatoryChoiceCanPopulateEmptyLibrary() {
        Permanent augermage = harness.addToBattlefieldAndReturn(player1, new SadisticAugermage());
        Card handCard = new GrayscaledGharial();
        harness.setHand(player1, List.of(new LastGasp(), handCard));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of());

        kill(augermage);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);

        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void kill(Permanent augermage) {
        kill(augermage, player1);
    }

    private void kill(Permanent augermage, Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, augermage.getId());
        resolveAllTriggers();
    }
}
