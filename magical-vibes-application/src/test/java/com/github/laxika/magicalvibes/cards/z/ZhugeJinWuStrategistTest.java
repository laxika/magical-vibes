package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZhugeJinWuStrategistTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature becomes unblockable when the ability resolves")
    void makesTargetUnblockable() {
        setupZhugeJinOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at cleanup")
    void unblockableWearsOff() {
        setupZhugeJinOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Taps Zhuge Jin when the ability is activated")
    void tapsOnActivation() {
        setupZhugeJinOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Zhuge Jin, Wu Strategist").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate during the beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupZhugeJinOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupZhugeJinOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.addToBattlefield(player1, new ZhugeJinWuStrategist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        findPermanent(player1, "Zhuge Jin, Wu Strategist").setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private void setupZhugeJinOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new ZhugeJinWuStrategist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        findPermanent(player1, "Zhuge Jin, Wu Strategist").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
