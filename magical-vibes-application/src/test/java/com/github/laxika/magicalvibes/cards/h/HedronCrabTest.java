package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HedronCrab.class, Forest.class})
class HedronCrabTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall mills three cards from the targeted opponent's library")
    void landfallMillsTargetOpponent() {
        harness.addToBattlefield(player1, new HedronCrab());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Landfall can target the controller")
    void landfallCanTargetController() {
        harness.addToBattlefield(player1, new HedronCrab());
        harness.setLibrary(player1, libraryWithFiveCards());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Hedron Crab")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new HedronCrab());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Landfall mills the remaining cards when fewer than three are available")
    void landfallMillsShortLibrary() {
        harness.addToBattlefield(player1, new HedronCrab());
        List<Card> library = List.of(new Forest(), new Forest());
        harness.setLibrary(player2, library);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Landfall can target a player whose library is empty")
    void landfallResolvesWithEmptyLibrary() {
        harness.addToBattlefield(player1, new HedronCrab());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land entering without being played triggers landfall and mills the top cards")
    void landEnteringWithoutBeingPlayedTriggers() {
        harness.addToBattlefield(player1, new HedronCrab());
        List<Card> library = libraryWithFiveCards();
        harness.setLibrary(player2, library);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library.subList(0, 3));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library.subList(3, 5));
    }

    @Test
    @DisplayName("A landfall trigger still resolves after Hedron Crab leaves the battlefield")
    void landfallResolvesAfterSourceLeaves() {
        var crab = harness.addToBattlefieldAndReturn(player1, new HedronCrab());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(crab);
        gd.playerGraveyards.get(player1.getId()).add(crab.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    private List<Card> libraryWithFiveCards() {
        return List.of(
                new Forest(),
                new Forest(),
                new Forest(),
                new Forest(),
                new Forest()
        );
    }
}
