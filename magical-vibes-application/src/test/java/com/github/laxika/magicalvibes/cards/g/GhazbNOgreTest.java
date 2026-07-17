package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GhazbNOgreTest extends BaseCardTest {

    private static final String OGRE = "Ghazbán Ogre";

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    @Test
    @DisplayName("Player with strictly the most life gains control during controller's upkeep")
    void mostLifePlayerGainsControl() {
        harness.addToBattlefield(player1, new GhazbNOgre());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals(OGRE));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals(OGRE));
    }

    @Test
    @DisplayName("Controller keeps the creature when they have the most life")
    void controllerKeepsWhenHighest() {
        harness.addToBattlefield(player1, new GhazbNOgre());
        harness.setLife(player1, 25);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals(OGRE));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals(OGRE));
    }

    @Test
    @DisplayName("No control change when players are tied for the most life")
    void noChangeOnTie() {
        harness.addToBattlefield(player1, new GhazbNOgre());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals(OGRE));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals(OGRE));
    }
}
