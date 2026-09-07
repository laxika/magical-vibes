package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SadisticAugermage.class, Murder.class, GrizzlyBears.class, Peek.class})
class SadisticAugermageTest extends BaseCardTest {

    @Test
    @DisplayName("When Sadistic Augermage dies, each player puts a card from hand on top of their library")
    void eachPlayerPutsAHandCardOnTop() {
        Permanent augermage = harness.addToBattlefieldAndReturn(player1, new SadisticAugermage());
        Card player1Card = new GrizzlyBears();
        Card player2Card = new Peek();
        Card player1OldTop = new Peek();
        Card player2OldTop = new GrizzlyBears();
        Card murder = new Murder();
        harness.setHand(player1, new ArrayList<>(List.of(murder, player1Card)));
        harness.setHand(player2, new ArrayList<>(List.of(player2Card)));
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

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).startsWith(player1Card, player1OldTop);
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(player2Card, player2OldTop);
    }

    @Test
    @DisplayName("A player with an empty hand does not get a choice")
    void emptyHandsDoNotPrompt() {
        Permanent augermage = harness.addToBattlefieldAndReturn(player1, new SadisticAugermage());
        Card murder = new Murder();
        harness.setHand(player1, new ArrayList<>(List.of(murder)));
        harness.setHand(player2, List.of());

        kill(augermage);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void kill(Permanent augermage) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, augermage.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
