package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrimalOrderTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    @Test
    @DisplayName("Deals damage to active player equal to their nonbasic lands")
    void damagesActivePlayerByNonbasicLandCount() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player1, new FieldOfRuin());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Basic lands are not counted")
    void basicLandsDoNotCount() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("On opponent's upkeep, damages the opponent by their own nonbasic lands")
    void damagesOpponentByTheirNonbasicLands() {
        harness.addToBattlefield(player1, new PrimalOrder());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player1, new FieldOfRuin());
        int p1LifeBefore = gd.getLife(player1.getId());
        int p2LifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        // Only player2's single nonbasic land counts; controller is untouched.
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore);
    }
}
