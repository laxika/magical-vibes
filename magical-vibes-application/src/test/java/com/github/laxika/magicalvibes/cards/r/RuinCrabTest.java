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
