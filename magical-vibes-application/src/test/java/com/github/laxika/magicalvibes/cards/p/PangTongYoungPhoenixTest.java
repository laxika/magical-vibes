package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlertShuInfantry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PangTongYoungPhoenix.class, AlertShuInfantry.class})
class PangTongYoungPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +0/+2 until end of turn when the ability resolves")
    void boostsTargetCreature() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Alert Shu Infantry");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent infantry = findPermanent(player1, "Alert Shu Infantry");
        assertThat(infantry.getPowerModifier()).isEqualTo(0);
        assertThat(infantry.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Alert Shu Infantry");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        Permanent infantry = findPermanent(player1, "Alert Shu Infantry");
        assertThat(infantry.getPowerModifier()).isEqualTo(0);
        assertThat(infantry.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Taps Pang Tong when the ability is activated")
    void tapsOnActivation() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Alert Shu Infantry");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Pang Tong, \"Young Phoenix\"").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate during the beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        UUID targetId = harness.getPermanentId(player1, "Alert Shu Infantry");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        UUID targetId = harness.getPermanentId(player1, "Alert Shu Infantry");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate before attackers in a second combat phase")
    void cannotActivateInSecondCombatPhase() {
        setupOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;
        UUID targetId = harness.getPermanentId(player1, "Alert Shu Infantry");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new PangTongYoungPhoenix());
        addCreatureReady(player1, new AlertShuInfantry());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player1, "Alert Shu Infantry");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void boostsOpponentsCreature() {
        addCreatureReady(player1, new PangTongYoungPhoenix());
        addCreatureReady(player2, new AlertShuInfantry());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Alert Shu Infantry");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent infantry = findPermanent(player2, "Alert Shu Infantry");
        assertThat(infantry.getPowerModifier()).isEqualTo(0);
        assertThat(infantry.getToughnessModifier()).isEqualTo(2);
    }

    private void setupOnMyTurn(TurnStep step) {
        addCreatureReady(player1, new PangTongYoungPhoenix());
        addCreatureReady(player1, new AlertShuInfantry());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
