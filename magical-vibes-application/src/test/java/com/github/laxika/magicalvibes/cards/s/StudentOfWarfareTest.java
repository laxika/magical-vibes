package com.github.laxika.magicalvibes.cards.s;

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

class StudentOfWarfareTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Student of Warfare's stats and combat keyword at each threshold")
    void levelsUpAtThresholds() {
        Permanent student = addCreatureReady(player1, new StudentOfWarfare());

        assertStats(student, 1, 1);
        assertThat(gqs.hasKeyword(gd, student, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, student, Keyword.DOUBLE_STRIKE)).isFalse();

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(student.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(student, 1, 1);
        assertThat(gqs.hasKeyword(gd, student, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, student, Keyword.DOUBLE_STRIKE)).isFalse();

        levelUp(player1);

        assertThat(student.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
        assertStats(student, 3, 3);
        assertThat(gqs.hasKeyword(gd, student, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, student, Keyword.DOUBLE_STRIKE)).isFalse();

        for (int i = 0; i < 4; i++) {
            levelUp(player1);
        }

        assertThat(student.getCounterCount(CounterType.LEVEL)).isEqualTo(6);
        assertStats(student, 3, 3);
        assertThat(gqs.hasKeyword(gd, student, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, student, Keyword.DOUBLE_STRIKE)).isFalse();

        levelUp(player1);

        assertThat(student.getCounterCount(CounterType.LEVEL)).isEqualTo(7);
        assertStats(student, 4, 4);
        assertThat(gqs.hasKeyword(gd, student, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, student, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent student = addCreatureReady(player1, new StudentOfWarfare());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> levelUp(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(student.getCounterCount(CounterType.LEVEL)).isZero();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.WHITE, 7);
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
