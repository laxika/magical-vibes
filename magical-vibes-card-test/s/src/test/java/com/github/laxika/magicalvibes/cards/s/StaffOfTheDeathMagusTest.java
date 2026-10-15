package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KalonianTusker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StaffOfTheDeathMagus.class, ChildOfNight.class, DoomBlade.class, KalonianTusker.class, Swamp.class, Island.class})
class StaffOfTheDeathMagusTest extends BaseCardTest {

    private void addStaff() {
        harness.addToBattlefield(player1, new StaffOfTheDeathMagus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Gains 1 life when you cast a black spell")
    void gainsLifeOnBlackSpell() {
        addStaff();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new ChildOfNight(), "{1}{B}");
        harness.passBothPriorities(); // resolve cast trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not gain life when you cast a non-black spell")
    void noLifeOnNonBlackSpell() {
        addStaff();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new KalonianTusker(), "{G}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Gains 1 life when a Swamp you control enters")
    void gainsLifeWhenSwampEnters() {
        addStaff();
        harness.setHand(player1, List.of(new Swamp()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not gain life when a non-Swamp land enters")
    void noLifeOnNonSwampLand() {
        addStaff();
        harness.setHand(player1, List.of(new Island()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent casting a black spell does not trigger")
    void opponentBlackSpellDoesNotTrigger() {
        addStaff();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new ChildOfNight(), "{1}{B}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent's Swamp entering does not trigger")
    void opponentSwampDoesNotTrigger() {
        addStaff();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Swamp()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Each Staff triggers separately before the black spell resolves")
    void multipleStaffsTriggerOnBlackSpell() {
        addStaff();
        harness.addToBattlefield(player1, new StaffOfTheDeathMagus());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new ChildOfNight(), "{1}{B}");

        assertThat(gd.stack).hasSize(3);
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 1);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Each Staff triggers separately when a Swamp enters")
    void multipleStaffsTriggerOnSwamp() {
        addStaff();
        harness.addToBattlefield(player1, new StaffOfTheDeathMagus());
        harness.setHand(player1, List.of(new Swamp()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Casting a black instant on an opponent's turn gains life before it resolves")
    void blackInstantOnOpponentTurnTriggers() {
        addStaff();
        var target = harness.addToBattlefieldAndReturn(player2, new KalonianTusker());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 1);
        harness.assertOnBattlefield(player2, "Kalonian Tusker");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Kalonian Tusker");
        harness.assertLife(player1, lifeBefore + 1);
    }
}
