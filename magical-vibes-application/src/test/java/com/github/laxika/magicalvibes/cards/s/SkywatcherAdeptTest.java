package com.github.laxika.magicalvibes.cards.s;

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

class SkywatcherAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Skywatcher Adept's base power and toughness at its thresholds")
    void levelsUpAtThresholds() {
        Permanent adept = addCreatureReady(player1, new SkywatcherAdept());

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(adept.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(adept, 2, 2);

        levelUp(player1);

        assertThat(adept.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
        assertStats(adept, 2, 2);

        levelUp(player1);

        assertThat(adept.getCounterCount(CounterType.LEVEL)).isEqualTo(3);
        assertStats(adept, 4, 2);
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent adept = addCreatureReady(player1, new SkywatcherAdept());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> levelUp(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(adept.getCounterCount(CounterType.LEVEL)).isZero();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 9);
    }

    private void levelUp(Player player) {
        harness.activateAbility(player, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
