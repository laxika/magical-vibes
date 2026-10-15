package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.w.WordsOfWar;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmosElixir.class, BeskirShieldmate.class, WordsOfWar.class})
class CosmosElixirTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card above the starting life total")
    void drawsAboveStartingLifeTotal() {
        harness.addToBattlefield(player1, new CosmosElixir());
        harness.setHand(player1, List.of());
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL + 1);
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));

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
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));

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
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));

        advanceToEndStep(player1);
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL - 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL + 1);
    }

    @Test
    void gainsLifeBelowStartingLifeTotal() {
        harness.addToBattlefield(player1, new CosmosElixir());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 18);
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsWhenLifeRisesAboveStartingTotalBeforeResolution() {
        harness.addToBattlefield(player1, new CosmosElixir());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player1, 21);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertInHand(player1, "Beskir Shieldmate");
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new CosmosElixir());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 21);
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 21);
    }

    @Test
    void gainsLifeAtCommanderStartingLifeTotal() {
        gd.format = DeckFormat.COMMANDER;
        harness.addToBattlefield(player1, new CosmosElixir());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 40);
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 42);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsAboveCommanderStartingLifeTotal() {
        gd.format = DeckFormat.COMMANDER;
        harness.addToBattlefield(player1, new CosmosElixir());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 41);
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 41);
        harness.assertInHand(player1, "Beskir Shieldmate");
    }

    @Test
    @CardUsed({CosmosElixir.class, BeskirShieldmate.class, WordsOfWar.class})
    void doesNotGainLifeWhenDrawReplacementLowersLifeTotal() {
        harness.addToBattlefield(player1, new WordsOfWar());
        harness.addToBattlefield(player1, new CosmosElixir());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 21);
        harness.setLibrary(player1, List.of(new BeskirShieldmate()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
