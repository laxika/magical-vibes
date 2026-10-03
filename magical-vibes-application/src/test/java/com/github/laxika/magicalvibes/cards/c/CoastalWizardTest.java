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
        addCreatureReady(player1, new CoastalWizard());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }

    @Test
    @DisplayName("Can activate in beginning of combat before attackers are declared")
    void canActivateAtBeginningOfCombat() {
        setupWizardOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        harness.addToBattlefield(player2, new RagingGoblin());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Raging Goblin"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Coastal Wizard");
        harness.assertInHand(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Can return another creature controlled by the Wizard's controller")
    void canTargetOwnCreature() {
        setupWizardOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new RagingGoblin());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Raging Goblin"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Coastal Wizard");
        harness.assertNotOnBattlefield(player1, "Raging Goblin");
        harness.assertInHand(player1, "Coastal Wizard");
        harness.assertInHand(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("Does not return the Wizard when its only target leaves before resolution")
    void illegalTargetPreventsSelfReturn() {
        setupWizardOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        var target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Coastal Wizard");
        harness.assertNotInHand(player1, "Coastal Wizard");
        assertThat(findPermanent(player1, "Coastal Wizard").isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Raging Goblin");
        harness.assertNotInHand(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Returns the target even if the Wizard leaves before resolution")
    void sourceLeavingDoesNotPreventTargetReturn() {
        setupWizardOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        var wizard = findPermanent(player1, "Coastal Wizard");
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Raging Goblin"));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, wizard));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Coastal Wizard");
        harness.assertNotInHand(player1, "Coastal Wizard");
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
        harness.assertInHand(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new CoastalWizard());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new RagingGoblin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Raging Goblin")))
                .isInstanceOf(IllegalStateException.class);
    }
}
