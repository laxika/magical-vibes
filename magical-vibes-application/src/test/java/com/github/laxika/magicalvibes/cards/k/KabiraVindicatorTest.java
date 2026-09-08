package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class KabiraVindicatorTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Kabira Vindicator and its other creatures anthem")
    void levelsUpAtThresholds() {
        Permanent vindicator = addCreatureReady(player1, new KabiraVindicator());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        assertStats(vindicator, 2, 4);
        assertStats(ownBear, 2, 2);
        assertStats(opposingBear, 2, 2);

        prepareForLeveling(player1);
        levelUp(player1, vindicator);

        assertThat(vindicator.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(vindicator, 2, 4);
        assertStats(ownBear, 2, 2);

        levelUp(player1, vindicator);

        assertThat(vindicator.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
        assertStats(vindicator, 3, 6);
        assertStats(ownBear, 3, 3);
        assertStats(opposingBear, 2, 2);

        for (int i = 0; i < 3; i++) {
            levelUp(player1, vindicator);
        }

        assertThat(vindicator.getCounterCount(CounterType.LEVEL)).isEqualTo(5);
        assertStats(vindicator, 4, 8);
        assertStats(ownBear, 4, 4);
        assertStats(opposingBear, 2, 2);
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent vindicator = addCreatureReady(player1, new KabiraVindicator());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> levelUp(player1, vindicator))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(vindicator.getCounterCount(CounterType.LEVEL)).isZero();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.WHITE, 5);
        harness.addMana(player, ManaColor.COLORLESS, 10);
    }

    private void levelUp(Player player, Permanent vindicator) {
        int permanentIndex = gd.playerBattlefields.get(player.getId()).indexOf(vindicator);
        harness.activateAbility(player, permanentIndex, 0, null, null);
        harness.passBothPriorities();
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
