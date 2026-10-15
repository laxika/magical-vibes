package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WorryBeads.class)
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

    @Test
    @DisplayName("Copies controlled by different players each mill the active player's top card")
    void multipleCopiesMillTopCardsInOrder() {
        harness.addToBattlefield(player1, new WorryBeads());
        harness.addToBattlefield(player2, new WorryBeads());
        WorryBeads first = new WorryBeads();
        WorryBeads second = new WorryBeads();
        WorryBeads third = new WorryBeads();
        harness.setLibrary(player2, List.of(first, second, third));
        int otherLibrarySize = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(otherLibrarySize);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling an empty library does not cause a player to lose")
    void emptyLibraryDoesNotCauseLoss() {
        harness.addToBattlefield(player1, new WorryBeads());
        harness.setLibrary(player2, List.of());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("An upkeep trigger still mills after Worry Beads leaves the battlefield")
    void triggerResolvesWithoutSource() {
        harness.addToBattlefield(player1, new WorryBeads());
        WorryBeads top = new WorryBeads();
        WorryBeads next = new WorryBeads();
        harness.setLibrary(player2, List.of(top, next));

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(next);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
