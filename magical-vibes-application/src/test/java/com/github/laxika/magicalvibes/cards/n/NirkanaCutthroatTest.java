package com.github.laxika.magicalvibes.cards.n;

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

class NirkanaCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Nirkana Cutthroat's stats and first strike at level three")
    void levelsUpAtThresholds() {
        Permanent cutthroat = addCreatureReady(player1, new NirkanaCutthroat());

        assertStats(cutthroat, 3, 2);
        assertThat(gqs.hasKeyword(gd, cutthroat, Keyword.FIRST_STRIKE)).isFalse();

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(cutthroat.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(cutthroat, 4, 3);
        assertThat(gqs.hasKeyword(gd, cutthroat, Keyword.FIRST_STRIKE)).isFalse();

        levelUp(player1);

        assertThat(cutthroat.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
        assertStats(cutthroat, 4, 3);
        assertThat(gqs.hasKeyword(gd, cutthroat, Keyword.FIRST_STRIKE)).isFalse();

        levelUp(player1);

        assertThat(cutthroat.getCounterCount(CounterType.LEVEL)).isEqualTo(3);
        assertStats(cutthroat, 5, 4);
        assertThat(gqs.hasKeyword(gd, cutthroat, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent cutthroat = addCreatureReady(player1, new NirkanaCutthroat());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(cutthroat.getCounterCount(CounterType.LEVEL)).isZero();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.BLACK, 9);
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
