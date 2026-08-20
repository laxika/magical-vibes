package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmosElixir.class, GrizzlyBears.class})
class CosmosElixirTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card above the starting life total")
    void drawsAboveStartingLifeTotal() {
        harness.addToBattlefield(player1, new CosmosElixir());
        harness.setHand(player1, List.of());
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL + 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL + 1);
    }

    @Test
    @DisplayName("Gains two life at or below the starting life total")
    void gainsTwoLifeAtOrBelowStartingLifeTotal() {
        harness.addToBattlefield(player1, new CosmosElixir());
        harness.setHand(player1, List.of());
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL + 2);
    }

    @Test
    @DisplayName("Uses the life total at resolution to choose the branch")
    void usesLifeTotalAtResolution() {
        harness.addToBattlefield(player1, new CosmosElixir());
        harness.setHand(player1, List.of());
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL + 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToEndStep(player1);
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL - 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL + 1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
