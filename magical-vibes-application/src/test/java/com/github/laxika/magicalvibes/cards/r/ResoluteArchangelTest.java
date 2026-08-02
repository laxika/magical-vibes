package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResoluteArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("Entering below starting life sets the controller's life total to starting life")
    void enterBelowStartingLifeSetsLifeToStartingLife() {
        harness.setLife(player1, 7);

        castResoluteArchangel();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL);
    }

    @Test
    @DisplayName("Entering at starting life leaves the controller's life total unchanged")
    void enterAtStartingLifeLeavesLifeUnchanged() {
        castResoluteArchangel();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL);
    }

    @Test
    @DisplayName("Entering above starting life leaves the controller's life total unchanged")
    void enterAboveStartingLifeLeavesLifeUnchanged() {
        harness.setLife(player1, 25);

        castResoluteArchangel();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Only the controller's life total changes")
    void onlyControllerLifeChanges() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 13);

        castResoluteArchangel();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    private void castResoluteArchangel() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ResoluteArchangel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
