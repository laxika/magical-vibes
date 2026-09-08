package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KazuulsTollCollector.class, StriderHarness.class})
class KazuulsTollCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches a controlled Equipment to itself")
    void attachesControlledEquipmentToItself() {
        Permanent collector = addReadyCollector(player1);
        Permanent equipment = addEquipment(player1);
        prepareSorcerySpeedActivation(player1);

        harness.activateAbility(player1, 0, null, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(collector.getId());
    }

    @Test
    @DisplayName("Cannot target an Equipment controlled by an opponent")
    void cannotTargetOpponentsEquipment() {
        addReadyCollector(player1);
        Permanent equipment = addEquipment(player2);
        prepareSorcerySpeedActivation(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only during its controller's main phase")
    void cannotActivateOutsideSorcerySpeed() {
        addReadyCollector(player1);
        Permanent equipment = addEquipment(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCollector(Player player) {
        return addCreatureReady(player, new KazuulsTollCollector());
    }

    private Permanent addEquipment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new StriderHarness());
    }

    private void prepareSorcerySpeedActivation(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
