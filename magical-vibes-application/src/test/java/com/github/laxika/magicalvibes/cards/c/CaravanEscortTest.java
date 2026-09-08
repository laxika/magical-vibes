package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaravanEscortTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Caravan Escort's base power and toughness at each threshold")
    void levelsUpAtThresholds() {
        Permanent escort = addEscortReady(player1);

        assertThat(gqs.getEffectivePower(gd, escort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, escort)).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        levelUp(player1);
        assertThat(escort.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, escort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, escort)).isEqualTo(2);

        for (int i = 0; i < 4; i++) {
            levelUp(player1);
        }

        assertThat(escort.getCounterCount(CounterType.LEVEL)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, escort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, escort)).isEqualTo(5);
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        addEscortReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> levelUp(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addEscortReady(Player player) {
        return addCreatureReady(player, new CaravanEscort());
    }

    private void levelUp(Player player) {
        harness.activateAbility(player, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
