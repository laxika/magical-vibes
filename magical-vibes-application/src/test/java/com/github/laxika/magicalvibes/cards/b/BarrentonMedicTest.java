package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BarrentonMedicTest extends BaseCardTest {

    // ===== Prevent ability =====

    @Test
    @DisplayName("Tap ability sets the global damage prevention shield")
    void tapAbilityPreventsDamage() {
        addReadyMedic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getGameData().globalDamagePreventionShield).isEqualTo(1);
    }

    // ===== Untap ability =====

    @Test
    @DisplayName("Untap ability untaps the Medic")
    void untapAbilityUntapsMedic() {
        Permanent medic = addReadyMedic(player1);
        medic.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(medic.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap ability puts a -1/-1 counter on the Medic as a cost (paid on activation)")
    void untapAbilityPutsMinusCounterOnActivation() {
        Permanent medic = addReadyMedic(player1);
        medic.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);

        // Cost is paid immediately on activation, before the ability resolves.
        assertThat(medic.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each untap activation stacks another -1/-1 counter")
    void untapAbilityStacksCounters() {
        Permanent medic = addReadyMedic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(medic.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addReadyMedic(Player player) {
        BarrentonMedic card = new BarrentonMedic();
        Permanent medic = new Permanent(card);
        medic.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(medic);
        return medic;
    }
}
