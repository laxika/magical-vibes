package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XunYuWeiAdvisorTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you control gets +2/+0 until end of turn when the ability resolves")
    void boostsTargetCreature() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Taps Xun Yu when the ability is activated")
    void tapsOnActivation() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Xun Yu, Wei Advisor").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.addToBattlefield(player1, new XunYuWeiAdvisor());
        harness.addToBattlefield(player1, new GrizzlyBears());
        findPermanent(player1, "Xun Yu, Wei Advisor").setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentTargetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentTargetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new XunYuWeiAdvisor());
        harness.addToBattlefield(player1, new GrizzlyBears());
        findPermanent(player1, "Xun Yu, Wei Advisor").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
