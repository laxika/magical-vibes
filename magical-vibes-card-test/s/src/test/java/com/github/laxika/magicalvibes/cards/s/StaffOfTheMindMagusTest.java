package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeadlyRecluse;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StaffOfTheMindMagus.class, SeacoastDrake.class, DeadlyRecluse.class,
        Island.class, Mountain.class})
class StaffOfTheMindMagusTest extends BaseCardTest {

    private void addStaff() {
        harness.addToBattlefield(player1, new StaffOfTheMindMagus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Gains 1 life when you cast a blue spell")
    void gainsLifeOnBlueSpell() {
        addStaff();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new SeacoastDrake(), "{1}{U}");
        harness.passBothPriorities(); // resolve cast trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not gain life when you cast a non-blue spell")
    void noLifeOnNonBlueSpell() {
        addStaff();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new DeadlyRecluse(), "{1}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Gains 1 life when an Island you control enters")
    void gainsLifeWhenIslandEnters() {
        addStaff();
        harness.setHand(player1, List.of(new Island()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not gain life when a non-Island land enters")
    void noLifeOnNonIslandLand() {
        addStaff();
        harness.setHand(player1, List.of(new Mountain()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent casting a blue spell does not trigger")
    void opponentBlueSpellDoesNotTrigger() {
        addStaff();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new SeacoastDrake(), "{1}{U}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent's Island entering does not trigger")
    void opponentIslandDoesNotTrigger() {
        addStaff();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Island()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("An Island entering without a land play triggers")
    void islandEnteringWithoutBeingPlayedTriggers() {
        addStaff();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new Island());

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("A blue creature entering without being cast does not trigger")
    void blueCreatureEnteringWithoutCastDoesNotTrigger() {
        addStaff();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new SeacoastDrake());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Each Staff triggers separately for a blue spell")
    void multipleStaffsTriggerOnBlueSpell() {
        addStaff();
        harness.addToBattlefield(player1, new StaffOfTheMindMagus());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new SeacoastDrake(), "{1}{U}");

        assertThat(gd.stack).hasSize(3);
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 1);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Each Staff triggers separately for an Island")
    void multipleStaffsTriggerOnIsland() {
        addStaff();
        harness.addToBattlefield(player1, new StaffOfTheMindMagus());
        harness.setHand(player1, List.of(new Island()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 1);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }
}
