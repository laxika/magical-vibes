package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterOfTheFeast.class, GrizzlyBears.class, Shock.class})
class MasterOfTheFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent draws a card at your upkeep")
    void eachOpponentDrawsAtYourUpkeep() {
        harness.addToBattlefield(player1, new MasterOfTheFeast());
        Card opponentCard = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).addFirst(opponentCard);

        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCard);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new MasterOfTheFeast());
        Card controllerCard = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(controllerCard);

        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        assertThat(gd.playerDecks.get(player1.getId())).contains(controllerCard);
    }

    @Test
    @DisplayName("The opponent of player two draws during player two's upkeep")
    void drawsForOpponentOfSecondPlayer() {
        harness.addToBattlefield(player2, new MasterOfTheFeast());
        Card drawnCard = new MasterOfTheFeast();
        harness.setLibrary(player1, List.of(drawnCard));
        int controllerHandBefore = gd.playerHands.get(player2.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandBefore);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandBefore + 1).contains(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore);
    }

    @Test
    @DisplayName("Each copy triggers separately and draws one card")
    void multipleCopiesDrawSeparately() {
        harness.addToBattlefield(player1, new MasterOfTheFeast());
        harness.addToBattlefield(player1, new MasterOfTheFeast());
        Card firstCard = new MasterOfTheFeast();
        Card secondCard = new MasterOfTheFeast();
        harness.setLibrary(player2, List.of(firstCard, secondCard));
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1).contains(firstCard);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2)
                .contains(firstCard, secondCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
    }

    @Test
    @DisplayName("The upkeep trigger resolves after its source leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Card source = new MasterOfTheFeast();
        harness.addToBattlefield(player1, source);
        Card drawnCard = new MasterOfTheFeast();
        harness.setLibrary(player2, List.of(drawnCard));
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(source));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1).contains(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
    }
}
