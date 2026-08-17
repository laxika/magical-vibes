package com.github.laxika.magicalvibes.cards.b;

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

class BeastbreakerOfBalaGedTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Beastbreaker of Bala Ged's stats and trample at level four")
    void levelsUpAtThresholds() {
        Permanent beastbreaker = addCreatureReady(player1, new BeastbreakerOfBalaGed());

        assertStats(beastbreaker, 2, 2);
        assertThat(gqs.hasKeyword(gd, beastbreaker, Keyword.TRAMPLE)).isFalse();

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(beastbreaker.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(beastbreaker, 4, 4);
        assertThat(gqs.hasKeyword(gd, beastbreaker, Keyword.TRAMPLE)).isFalse();

        levelUp(player1);
        levelUp(player1);

        assertThat(beastbreaker.getCounterCount(CounterType.LEVEL)).isEqualTo(3);
        assertStats(beastbreaker, 4, 4);
        assertThat(gqs.hasKeyword(gd, beastbreaker, Keyword.TRAMPLE)).isFalse();

        levelUp(player1);

        assertThat(beastbreaker.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
        assertStats(beastbreaker, 6, 6);
        assertThat(gqs.hasKeyword(gd, beastbreaker, Keyword.TRAMPLE)).isTrue();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.GREEN, 4);
        harness.addMana(player, ManaColor.COLORLESS, 8);
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
