package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PangTongYoungPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +0/+2 until end of turn when the ability resolves")
    void boostsTargetCreature() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
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
    @DisplayName("Taps Pang Tong when the ability is activated")
    void tapsOnActivation() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Pang Tong, \"Young Phoenix\"").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate during the beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.stack).hasSize(1);
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
        harness.addToBattlefield(player1, new PangTongYoungPhoenix());
        harness.addToBattlefield(player1, new GrizzlyBears());
        findPermanent(player1, "Pang Tong, \"Young Phoenix\"").setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private void setupOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new PangTongYoungPhoenix());
        harness.addToBattlefield(player1, new GrizzlyBears());
        findPermanent(player1, "Pang Tong, \"Young Phoenix\"").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
