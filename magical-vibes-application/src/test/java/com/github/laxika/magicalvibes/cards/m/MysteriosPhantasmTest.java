package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysteriosPhantasm.class})
class MysteriosPhantasmTest extends BaseCardTest {

    @Test
    void attackingMillsOneCard() {
        MysteriosPhantasm libraryCard = new MysteriosPhantasm();
        harness.setLibrary(player1, List.of(libraryCard));
        Permanent phantasm = addCreatureReady(player1, new MysteriosPhantasm());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(phantasm)));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void millsOnlyTheTopCardAndLeavesOpponentsLibraryAlone() {
        MysteriosPhantasm topCard = new MysteriosPhantasm();
        MysteriosPhantasm nextCard = new MysteriosPhantasm();
        MysteriosPhantasm opponentCard = new MysteriosPhantasm();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentCard));
        addCreatureReady(player1, new MysteriosPhantasm());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void playerTwoAttackingMillsPlayerTwosLibrary() {
        MysteriosPhantasm playerOneCard = new MysteriosPhantasm();
        MysteriosPhantasm playerTwoCard = new MysteriosPhantasm();
        harness.setLibrary(player1, List.of(playerOneCard));
        harness.setLibrary(player2, List.of(playerTwoCard));
        addCreatureReady(player2, new MysteriosPhantasm());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(playerOneCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(playerTwoCard);
    }

    @Test
    void attackingWithEmptyLibraryDoesNotCauseALoss() {
        harness.setLibrary(player1, List.of());
        addCreatureReady(player1, new MysteriosPhantasm());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void eachAttackingPhantasmMillsOneCard() {
        MysteriosPhantasm firstCard = new MysteriosPhantasm();
        MysteriosPhantasm secondCard = new MysteriosPhantasm();
        MysteriosPhantasm thirdCard = new MysteriosPhantasm();
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        addCreatureReady(player1, new MysteriosPhantasm());
        addCreatureReady(player1, new MysteriosPhantasm());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdCard);
    }
}
