package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SibsigHost.class})
class SibsigHostTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards from each player's library")
    void etbMillsThreeCardsFromEachPlayer() {
        int playerDeckSize = gd.playerDecks.get(player1.getId()).size();
        int opponentDeckSize = gd.playerDecks.get(player2.getId()).size();

        harness.castFromHand(player1, new SibsigHost(), "{4}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(playerDeckSize - 3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSize - 3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("ETB puts exactly the top three cards of each library into their owner's graveyard")
    void millsOnlyTopThreeCards() {
        List<SibsigHost> controllerLibrary = List.of(new SibsigHost(), new SibsigHost(),
                new SibsigHost(), new SibsigHost());
        List<SibsigHost> opponentLibrary = List.of(new SibsigHost(), new SibsigHost(),
                new SibsigHost(), new SibsigHost());
        harness.setLibrary(player1, controllerLibrary);
        harness.setLibrary(player2, opponentLibrary);

        harness.castFromHand(player1, new SibsigHost(), "{4}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(controllerLibrary);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(controllerLibrary.subList(0, 3));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrderElementsOf(opponentLibrary.subList(0, 3));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerLibrary.get(3));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibrary.get(3));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A short library mills all remaining cards without preventing the other player from milling")
    void shortLibraryDoesNotPreventOpponentMill() {
        List<SibsigHost> controllerLibrary = List.of(new SibsigHost(), new SibsigHost());
        List<SibsigHost> opponentLibrary = List.of(new SibsigHost(), new SibsigHost(), new SibsigHost());
        harness.setLibrary(player1, controllerLibrary);
        harness.setLibrary(player2, opponentLibrary);

        harness.castFromHand(player1, new SibsigHost(), "{4}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(controllerLibrary);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrderElementsOf(opponentLibrary);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("An empty opposing library does not prevent the controller from milling")
    void emptyOpponentLibraryDoesNotPreventControllerMill() {
        List<SibsigHost> controllerLibrary = List.of(new SibsigHost(), new SibsigHost(), new SibsigHost());
        harness.setLibrary(player1, controllerLibrary);
        harness.setLibrary(player2, List.of());

        harness.castFromHand(player1, new SibsigHost(), "{4}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(controllerLibrary);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }
}
