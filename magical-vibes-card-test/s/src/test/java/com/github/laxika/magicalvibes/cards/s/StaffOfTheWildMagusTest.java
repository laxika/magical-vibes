package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DeadlyRecluse;
import com.github.laxika.magicalvibes.cards.d.DragonHatchling;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StaffOfTheWildMagus.class, DeadlyRecluse.class, DragonHatchling.class, Forest.class, Island.class})
class StaffOfTheWildMagusTest extends BaseCardTest {

    private void addStaff() {
        harness.addToBattlefield(player1, new StaffOfTheWildMagus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Gains 1 life when you cast a green spell")
    void gainsLifeOnGreenSpell() {
        addStaff();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new DeadlyRecluse(), "{1}{G}");
        harness.passBothPriorities(); // resolve cast trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not gain life when you cast a non-green spell")
    void noLifeOnNonGreenSpell() {
        addStaff();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new DragonHatchling(), "{1}{R}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Gains 1 life when a Forest you control enters")
    void gainsLifeWhenForestEnters() {
        addStaff();
        harness.setHand(player1, List.of(new Forest()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not gain life when a non-Forest land enters")
    void noLifeOnNonForestLand() {
        addStaff();
        harness.setHand(player1, List.of(new Island()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent casting a green spell does not trigger")
    void opponentGreenSpellDoesNotTrigger() {
        addStaff();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();


        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new DeadlyRecluse(), "{1}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void opponentForestDoesNotTrigger() {
        addStaff();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void greenCreatureEnteringWithoutBeingCastDoesNotTrigger() {
        addStaff();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new DeadlyRecluse());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void forestEnteringWithoutLandPlayTriggers() {
        addStaff();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    void eachStaffTriggersSeparatelyForGreenSpell() {
        addStaff();
        harness.addToBattlefield(player1, new StaffOfTheWildMagus());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new DeadlyRecluse(), "{1}{G}");
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 1);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 2);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    void queuedTriggerResolvesAfterStaffLeavesBattlefield() {
        addStaff();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new DeadlyRecluse(), "{1}{G}");
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
    }
}
