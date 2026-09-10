package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LadySun.class, ForestBear.class, Forest.class})
class LadySunTest extends BaseCardTest {

    @Test
    @DisplayName("Returns Lady Sun and another target creature to their owners' hands")
    void bouncesSelfAndTarget() {
        setupLadySunOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new ForestBear());
        UUID targetId = harness.getPermanentId(player2, "Forest Bear");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lady Sun");
        harness.assertInHand(player1, "Lady Sun");
        harness.assertNotOnBattlefield(player2, "Forest Bear");
        harness.assertInHand(player2, "Forest Bear");
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetSelf() {
        setupLadySunOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID selfId = harness.getPermanentId(player1, "Lady Sun");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, selfId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        setupLadySunOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");
    }

    @Test
    @DisplayName("Can activate during the beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupLadySunOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        harness.addToBattlefield(player1, new ForestBear());
        UUID targetId = harness.getPermanentId(player1, "Forest Bear");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Lady Sun").isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupLadySunOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        harness.addToBattlefield(player2, new ForestBear());
        UUID targetId = harness.getPermanentId(player2, "Forest Bear");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate before attackers in a second combat phase")
    void cannotActivateInSecondCombatPhase() {
        setupLadySunOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;
        harness.addToBattlefield(player1, new ForestBear());
        UUID targetId = harness.getPermanentId(player1, "Forest Bear");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        setupLadySunOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player2, new ForestBear());
        UUID targetId = harness.getPermanentId(player2, "Forest Bear");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private void setupLadySunOnMyTurn(TurnStep step) {
        addCreatureReady(player1, new LadySun());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
