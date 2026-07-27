package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoastalWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Returns this creature and another target creature to their owners' hands")
    void bouncesSelfAndTarget() {
        setupWizardOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Coastal Wizard");
        harness.assertInHand(player1, "Coastal Wizard");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetSelf() {
        setupWizardOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID selfId = harness.getPermanentId(player1, "Coastal Wizard");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, selfId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupWizardOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    private void setupWizardOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new CoastalWizard());
        findPermanent(player1, "Coastal Wizard").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
