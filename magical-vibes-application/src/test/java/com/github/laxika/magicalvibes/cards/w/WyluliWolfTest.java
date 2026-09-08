package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WyluliWolf.class)
class WyluliWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +1/+1 until end of turn when the ability resolves")
    void boostsTargetCreature() {
        setupWolf();
        UUID targetId = harness.getPermanentId(player1, "Wyluli Wolf");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent wolf = findPermanent(player1, "Wyluli Wolf");
        assertThat(wolf.getPowerModifier()).isEqualTo(1);
        assertThat(wolf.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps the Wolf when activated")
    void tapsOnActivation() {
        setupWolf();
        UUID targetId = harness.getPermanentId(player1, "Wyluli Wolf");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Wyluli Wolf").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the ability while the Wolf is already tapped")
    void cannotActivateWhenTapped() {
        setupWolf();
        UUID targetId = harness.getPermanentId(player1, "Wyluli Wolf");

        harness.activateAbility(player1, 0, null, targetId);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void boostsOpponentsCreature() {
        setupWolf();
        Permanent opponentWolf = addCreatureReady(player2, new WyluliWolf());

        harness.activateAbility(player1, 0, null, opponentWolf.getId());
        harness.passBothPriorities();

        assertThat(opponentWolf.getPowerModifier()).isEqualTo(1);
        assertThat(opponentWolf.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupWolf();
        UUID targetId = harness.getPermanentId(player1, "Wyluli Wolf");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent wolf = findPermanent(player1, "Wyluli Wolf");
        assertThat(wolf.getPowerModifier()).isEqualTo(0);
        assertThat(wolf.getToughnessModifier()).isEqualTo(0);
    }

    private void setupWolf() {
        addCreatureReady(player1, new WyluliWolf());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
