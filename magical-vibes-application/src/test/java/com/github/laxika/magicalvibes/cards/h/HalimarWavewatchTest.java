package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HalimarWavewatchTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Halimar Wavewatch's base power, toughness, and islandwalk")
    void levelsUpAtThresholds() {
        Permanent wavewatch = addCreatureReady(player1, new HalimarWavewatch());

        assertStats(wavewatch, 0, 3);
        assertThat(gqs.hasKeyword(gd, wavewatch, Keyword.ISLANDWALK)).isFalse();

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(wavewatch.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(wavewatch, 0, 6);
        assertThat(gqs.hasKeyword(gd, wavewatch, Keyword.ISLANDWALK)).isFalse();

        for (int i = 0; i < 3; i++) {
            levelUp(player1);
        }

        assertThat(wavewatch.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
        assertStats(wavewatch, 0, 6);
        assertThat(gqs.hasKeyword(gd, wavewatch, Keyword.ISLANDWALK)).isFalse();

        levelUp(player1);

        assertThat(wavewatch.getCounterCount(CounterType.LEVEL)).isEqualTo(5);
        assertStats(wavewatch, 6, 6);
        assertThat(gqs.hasKeyword(gd, wavewatch, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent wavewatch = addCreatureReady(player1, new HalimarWavewatch());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> levelUp(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(wavewatch.getCounterCount(CounterType.LEVEL)).isZero();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 10);
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
