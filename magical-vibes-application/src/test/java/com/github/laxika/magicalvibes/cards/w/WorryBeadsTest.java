package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorryBeadsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's upkeep mills one card from that player's library")
    void eachPlayersUpkeepMillsThatPlayer() {
        harness.addToBattlefield(player1, new WorryBeads());
        int player1DeckSize = gd.playerDecks.get(player1.getId()).size();
        int player2DeckSize = gd.playerDecks.get(player2.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckSize - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckSize);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckSize - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
