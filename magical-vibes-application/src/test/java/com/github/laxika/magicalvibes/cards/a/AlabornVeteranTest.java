package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlabornVeteran.class, AlabornGrenadier.class})
class AlabornVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+2 until end of turn when the ability resolves")
    void boostsTargetCreature() {
        setupVeteranOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Alaborn Grenadier");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent grenadier = findPermanent(player1, "Alaborn Grenadier");
        assertThat(grenadier.getPowerModifier()).isEqualTo(2);
        assertThat(grenadier.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void boostsOpponentsCreature() {
        setupVeteranOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent opponentGrenadier = addCreatureReady(player2, new AlabornGrenadier());

        harness.activateAbility(player1, 0, null, opponentGrenadier.getId());
        harness.passBothPriorities();

        assertThat(opponentGrenadier.getPowerModifier()).isEqualTo(2);
        assertThat(opponentGrenadier.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost a target creature that leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        setupVeteranOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Alaborn Grenadier");

        harness.activateAbility(player1, 0, null, targetId);
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(targetId));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupVeteranOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Alaborn Grenadier");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent grenadier = findPermanent(player1, "Alaborn Grenadier");
        assertThat(grenadier.getPowerModifier()).isEqualTo(0);
        assertThat(grenadier.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Taps the veteran when the ability is activated")
    void tapsOnActivation() {
        setupVeteranOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Alaborn Grenadier");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Alaborn Veteran").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate during the beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupVeteranOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        UUID targetId = harness.getPermanentId(player1, "Alaborn Grenadier");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate during the declare attackers step")
    void cannotActivateAfterAttackersDeclared() {
        setupVeteranOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        UUID targetId = harness.getPermanentId(player1, "Alaborn Grenadier");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new AlabornVeteran());
        addCreatureReady(player1, new AlabornGrenadier());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player1, "Alaborn Grenadier");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Can target itself")
    void boostsItself() {
        setupVeteranOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent veteran = findPermanent(player1, "Alaborn Veteran");

        harness.activateAbility(player1, 0, null, veteran.getId());
        harness.passBothPriorities();

        assertThat(veteran.getPowerModifier()).isEqualTo(2);
        assertThat(veteran.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        setupVeteranOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = findPermanent(player1, "Alaborn Grenadier");

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Alaborn Veteran"));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void cannotActivateWhileTapped() {
        setupVeteranOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Alaborn Grenadier");
        harness.activateAbility(player1, 0, null, targetId);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        setupVeteranOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent veteran = findPermanent(player1, "Alaborn Veteran");
        veteran.setSummoningSick(true);
        UUID targetId = harness.getPermanentId(player1, "Alaborn Grenadier");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(veteran.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate during the postcombat main phase")
    void cannotActivatePostcombat() {
        setupVeteranOnMyTurn(TurnStep.POSTCOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Alaborn Grenadier");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate before attackers in a second combat phase")
    void cannotActivateInSecondCombat() {
        setupVeteranOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;
        UUID targetId = harness.getPermanentId(player1, "Alaborn Grenadier");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
        assertThat(gd.stack).isEmpty();
    }

    private void setupVeteranOnMyTurn(TurnStep step) {
        addCreatureReady(player1, new AlabornVeteran());
        addCreatureReady(player1, new AlabornGrenadier());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
