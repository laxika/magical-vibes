package com.github.laxika.magicalvibes.cards.k;

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

class KnightOfCliffhavenTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Knight of Cliffhaven's base power, toughness, and vigilance")
    void levelsUpAtThresholds() {
        Permanent knight = addCreatureReady(player1, new KnightOfCliffhaven());

        assertStats(knight, 2, 2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isFalse();

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(knight.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(knight, 2, 3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isFalse();

        for (int i = 0; i < 3; i++) {
            levelUp(player1);
        }

        assertThat(knight.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
        assertStats(knight, 4, 4);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent knight = addCreatureReady(player1, new KnightOfCliffhaven());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> levelUp(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(knight.getCounterCount(CounterType.LEVEL)).isZero();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 12);
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
