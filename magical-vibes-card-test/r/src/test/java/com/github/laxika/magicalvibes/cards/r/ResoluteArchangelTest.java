package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResoluteArchangel.class})
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

    @Test
    @DisplayName("No ability triggers when life is already at the starting total")
    void doesNotTriggerAtStartingLife() {
        castUntilEntry();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing life after entry cannot enable an ability that did not trigger")
    void losingLifeAfterEntryDoesNotEnableTrigger() {
        castUntilEntry();
        harness.setLife(player1, 15);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("The life condition is checked again when the ability resolves")
    void lifeAboveStartingTotalBeforeResolutionIsUnchanged() {
        harness.setLife(player1, 7);
        castUntilEntry();
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player1, 25);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Commander life below twenty is restored to forty")
    void commanderLowLifeIsRestoredToForty() {
        gd.format = DeckFormat.COMMANDER;
        harness.setLife(player1, 7);

        castResoluteArchangel();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(40);
    }

    @Test
    @DisplayName("Commander life between twenty and forty is restored to forty")
    void commanderLifeAboveTwentyIsRestoredToForty() {
        gd.format = DeckFormat.COMMANDER;
        harness.setLife(player1, 30);

        castResoluteArchangel();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(40);
    }

    private void castUntilEntry() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ResoluteArchangel(), "{5}{W}{W}");
        harness.passBothPriorities();
    }

    private void castResoluteArchangel() {
        castUntilEntry();
        resolveAllTriggers();
    }
}
