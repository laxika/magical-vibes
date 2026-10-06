package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinCrab.class, Forest.class, GrizzlyBears.class})
class RuinCrabTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall mills three cards from each opponent's library")
    void landfallMillsEachOpponent() {
        harness.addToBattlefield(player1, new RuinCrab());
        harness.setLibrary(player1, libraryWithFiveCards());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Ruin Crab")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new RuinCrab());
        harness.setLibrary(player1, libraryWithFiveCards());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Landfall mills the top three cards only when its trigger resolves")
    void millsTopThreeOnResolution() {
        harness.addToBattlefield(player1, new RuinCrab());
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        harness.setLibrary(player2, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, second, third, fourth);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second, third);
    }

    @Test
    @DisplayName("Landfall mills all remaining cards from a library with fewer than three cards")
    void millsShortLibrary() {
        harness.addToBattlefield(player1, new RuinCrab());
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Landfall resolves harmlessly when an opponent's library is empty")
    void emptyLibraryDoesNotPreventResolution() {
        harness.addToBattlefield(player1, new RuinCrab());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Ruin Crab triggers independently for the same land")
    void multipleCrabsEachMillThree() {
        harness.addToBattlefield(player1, new RuinCrab());
        harness.addToBattlefield(player1, new RuinCrab());
        harness.setLibrary(player2, List.of(
                new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }

    private List<Card> libraryWithFiveCards() {
        return List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()
        );
    }
}
