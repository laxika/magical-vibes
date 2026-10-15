package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeenDuelist.class, GrizzlyBears.class, Shock.class, Plains.class})
class KeenDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("You and the target opponent reveal, lose life based on the other card, and put it into hand")
    void revealsAndLosesLifeBasedOnOtherCard() {
        harness.addToBattlefield(player1, new KeenDuelist());

        Card ownCard = new GrizzlyBears();
        Card opponentCard = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(ownCard);
        gd.playerDecks.get(player2.getId()).addFirst(opponentCard);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).contains(ownCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCard);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new KeenDuelist());

        int startingLife1 = gd.getLife(player1.getId());
        int startingLife2 = gd.getLife(player2.getId());

        advanceToUpkeep(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife2);
    }

    @Test
    void emptyControllerLibraryStillCausesControllerToLoseLife() {
        harness.addToBattlefield(player1, new KeenDuelist());
        Card opponentCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveUpkeep(player1, player2);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void emptyOpponentLibraryStillCausesOpponentToLoseLife() {
        harness.addToBattlefield(player1, new KeenDuelist());
        Card ownCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveUpkeep(player1, player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void bothLibrariesEmptyDoesNotCauseLifeLossOrDrawingFromAnEmptyLibrary() {
        harness.addToBattlefield(player1, new KeenDuelist());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveUpkeep(player1, player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void revealingALandCausesNoLifeLossForTheOtherPlayer() {
        harness.addToBattlefield(player1, new KeenDuelist());
        Card ownCard = new Plains();
        Card opponentCard = new Shock();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveUpkeep(player1, player2);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    void secondPlayerAsControllerUsesTheirOwnLibraryAndUpkeep() {
        harness.addToBattlefield(player2, new KeenDuelist());
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new Shock();
        harness.setLibrary(player2, List.of(ownCard));
        harness.setLibrary(player1, List.of(opponentCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveUpkeep(player2, player1);

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(ownCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(opponentCard);
    }

    @Test
    void controllerCannotBeChosenAsTheTargetOpponent() {
        harness.addToBattlefield(player1, new KeenDuelist());
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);

            assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                    .isInstanceOf(IllegalStateException.class);

            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
        });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bothPlayersLosingTheirRemainingLifeResultsInADraw() {
        harness.addToBattlefield(player1, new KeenDuelist());
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new Shock();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player1, 1);
        harness.setLife(player2, 2);

        resolveUpkeep(player1, player2);

        harness.assertLife(player1, 0);
        harness.assertLife(player2, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isNull();
    }

    private void resolveUpkeep(Player controller, Player opponent) {
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(controller);
            harness.handlePermanentChosen(controller, opponent.getId());
            harness.passBothPriorities();
        });
    }
}
