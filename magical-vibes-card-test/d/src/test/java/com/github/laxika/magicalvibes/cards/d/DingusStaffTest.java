package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InfernalTribute;
import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DingusStaff.class, CruelEdict.class, GrizzlyBears.class, InfernalTribute.class,
        NevinyrralsDisk.class})
class DingusStaffTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the creature's controller when their creature dies")
    void dealsToCreatureControllerWhenCreatureDies() {
        harness.addToBattlefield(player1, new DingusStaff());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(DingusStaff.class);

        harness.passBothPriorities(); // Resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to controller when their own creature dies")
    void dealsToSelfWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new DingusStaff());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(DingusStaff.class);

        harness.passBothPriorities(); // Resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Two Dingus Staffs each trigger when a creature dies")
    void twoStaffsEachTrigger() {
        harness.addToBattlefield(player1, new DingusStaff());
        harness.addToBattlefield(player1, new DingusStaff());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(se -> se.getCard() instanceof DingusStaff);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not trigger when a noncreature permanent is sacrificed")
    void doesNotTriggerWhenNonCreaturePermanentIsSacrificed() {
        Permanent tribute = harness.addToBattlefieldAndReturn(player1, new InfernalTribute());
        harness.addToBattlefield(player1, new DingusStaff());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, tribute.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each creature dying simultaneously damages its own controller")
    void simultaneousDeathsDamageEachController() {
        harness.addToBattlefield(player1, new DingusStaff());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Pending damage resolves after Dingus Staff is sacrificed")
    void pendingDamageSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new InfernalTribute());
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new DingusStaff());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, staff.getId());
        harness.assertNotOnBattlefield(player1, "Dingus Staff");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dingus Staff");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Triggers for creatures destroyed simultaneously with Dingus Staff")
    void triggersWhenDestroyedTogetherWithCreatures() {
        harness.addToBattlefield(player1, new DingusStaff());
        harness.addToBattlefield(player1, new NevinyrralsDisk());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dingus Staff");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
    }
}
