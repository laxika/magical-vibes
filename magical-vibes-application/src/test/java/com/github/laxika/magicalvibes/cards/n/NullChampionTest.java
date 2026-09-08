package com.github.laxika.magicalvibes.cards.n;

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

class NullChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Null Champion's base power and toughness at its thresholds")
    void levelsUpAtThresholds() {
        Permanent champion = addCreatureReady(player1, new NullChampion());

        assertStats(champion, 1, 1);

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(champion.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(champion, 4, 2);

        for (int i = 0; i < 2; i++) {
            levelUp(player1);
        }

        assertThat(champion.getCounterCount(CounterType.LEVEL)).isEqualTo(3);
        assertStats(champion, 4, 2);

        levelUp(player1);

        assertThat(champion.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
        assertStats(champion, 7, 3);
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent champion = addCreatureReady(player1, new NullChampion());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> levelUp(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(champion.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    @DisplayName("Paying {B} grants Null Champion a regeneration shield")
    void blackActivationGrantsRegenerationShield() {
        Permanent champion = addCreatureReady(player1, new NullChampion());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(champion.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Null Champion cannot level up without enough mana")
    void cannotLevelUpWithoutMana() {
        addCreatureReady(player1, new NullChampion());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
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
