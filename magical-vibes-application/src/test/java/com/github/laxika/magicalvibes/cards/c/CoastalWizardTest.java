package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoastalWizard.class, Forest.class, RagingGoblin.class})
class CoastalWizardTest extends BaseCardTest {

    @Test
    @DisplayName("Returns this creature and another target creature to their owners' hands")
    void bouncesSelfAndTarget() {
        setupWizardOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new RagingGoblin());
        UUID targetId = harness.getPermanentId(player2, "Raging Goblin");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Coastal Wizard");
        harness.assertInHand(player1, "Coastal Wizard");
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
        harness.assertInHand(player2, "Raging Goblin");
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
        harness.addToBattlefield(player2, new RagingGoblin());
        UUID targetId = harness.getPermanentId(player2, "Raging Goblin");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        setupWizardOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        setupWizardOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.forceActivePlayer(player2);
        UUID targetId = harness.getPermanentId(player2, "Raging Goblin");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Taps Coastal Wizard as the activation cost")
    void tapsOnActivation() {
        setupWizardOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new RagingGoblin());
        UUID targetId = harness.getPermanentId(player2, "Raging Goblin");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Coastal Wizard").isTapped()).isTrue();
    }

    private void setupWizardOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new CoastalWizard());
        findPermanent(player1, "Coastal Wizard").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
