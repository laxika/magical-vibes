package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RunedHalo;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeferisTutelage.class, Forest.class, GrizzlyBears.class, Island.class, RunedHalo.class})
class TeferisTutelageTest extends BaseCardTest {

    @Test
    @DisplayName("When you draw a card, target opponent mills two cards")
    void controllerDrawMillsOpponent() {
        harness.addToBattlefield(player1, new TeferisTutelage());
        harness.setLibrary(player2, List.of(new Island(), new Forest(), new GrizzlyBears()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Entering the battlefield draws a card, then discards a card")
    void entersAndLoots() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TeferisTutelage(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's draw does not trigger the mill")
    void opponentDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new TeferisTutelage());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        int opponentDeckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player2);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(opponentDeckSizeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Protection from Teferi's Tutelage prevents its mill ability from targeting the opponent")
    void protectedOpponentIsNotMilled() {
        harness.addToBattlefield(player1, new TeferisTutelage());
        harness.addToBattlefieldAndReturn(player2, new RunedHalo()).setChosenName("Teferi's Tutelage");
        Island top = new Island();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(top, second));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling two cards from a one-card library mills the available card")
    void shortLibraryMillsAvailableCard() {
        harness.addToBattlefield(player1, new TeferisTutelage());
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(remaining));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Each Tutelage triggers independently for the same draw")
    void twoTutelagesMillFourCards() {
        harness.addToBattlefield(player1, new TeferisTutelage());
        harness.addToBattlefield(player1, new TeferisTutelage());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        advanceToDraw(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
