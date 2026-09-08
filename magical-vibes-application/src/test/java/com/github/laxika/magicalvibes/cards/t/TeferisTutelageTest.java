package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeferisTutelageTest extends BaseCardTest {

    @Test
    @DisplayName("When you draw a card, target opponent mills two cards")
    void controllerDrawMillsOpponent() {
        harness.addToBattlefield(player1, new TeferisTutelage());
        setDeck(player2, List.of(new Island(), new Forest(), new GrizzlyBears()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Entering the battlefield draws a card, then discards a card")
    void entersAndLoots() {
        setDeck(player1, List.of(new Forest()));
        setDeck(player2, List.of(new Island(), new Island(), new Island()));
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
        setDeck(player1, List.of(new Island(), new Island()));
        int opponentDeckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player2);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(opponentDeckSizeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
