package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskMageAssemblyTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when there are no creatures on the battlefield")
    void sacrificesItselfWhenNoCreaturesRemain() {
        harness.addToBattlefield(player1, new TaskMageAssembly());
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Task Mage Assembly");
        harness.assertInGraveyard(player1, "Task Mage Assembly");
    }

    @Test
    @DisplayName("Any player may activate it at sorcery speed to damage a creature")
    void anyPlayerMayActivateIt() {
        harness.addToBattlefield(player1, new TaskMageAssembly());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(bearsId);
        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate it outside sorcery speed or target a noncreature")
    void enforcesTimingAndTargetRestrictions() {
        harness.addToBattlefield(player1, new TaskMageAssembly());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
