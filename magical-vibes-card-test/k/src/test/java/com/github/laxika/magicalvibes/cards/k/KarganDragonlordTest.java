package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarganDragonlord.class})
class KarganDragonlordTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Kargan Dragonlord's stats and grants trample at level eight")
    void levelsUpAtThresholds() {
        Permanent dragonlord = addCreatureReady(player1, new KarganDragonlord());

        for (int i = 0; i < 4; i++) {
            levelUp(player1);
        }

        assertThat(dragonlord.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
        assertStats(dragonlord, 4, 4);
        assertThat(gqs.hasKeyword(gd, dragonlord, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dragonlord, Keyword.TRAMPLE)).isFalse();

        for (int i = 0; i < 4; i++) {
            levelUp(player1);
        }

        assertThat(dragonlord.getCounterCount(CounterType.LEVEL)).isEqualTo(8);
        assertStats(dragonlord, 8, 8);
        assertThat(gqs.hasKeyword(gd, dragonlord, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dragonlord, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Kargan Dragonlord can boost its power until end of turn")
    void boostsPowerUntilEndOfTurn() {
        Permanent dragonlord = addCreatureReady(player1, new KarganDragonlord());
        dragonlord.setCounterCount(CounterType.LEVEL, 8);
        prepareForLeveling(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertStats(dragonlord, 9, 8);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertStats(dragonlord, 8, 8);
    }

    @Test
    @DisplayName("Flying starts at four levels and trample starts at eight, including after counters are removed")
    void abilitiesTrackLevelBoundaries() {
        Permanent dragonlord = addCreatureReady(player1, new KarganDragonlord());
        for (int level : new int[]{0, 3, 4, 7, 8, 9, 7, 3}) {
            dragonlord.setCounterCount(CounterType.LEVEL, level);
            assertThat(gqs.hasKeyword(gd, dragonlord, Keyword.FLYING)).isEqualTo(level >= 4);
            assertThat(gqs.hasKeyword(gd, dragonlord, Keyword.TRAMPLE)).isEqualTo(level >= 8);
            assertStats(dragonlord, level >= 8 ? 8 : level >= 4 ? 4 : 2,
                    level >= 8 ? 8 : level >= 4 ? 4 : 2);
        }
    }

    @Test
    @DisplayName("The power boost cannot be activated below level eight")
    void cannotBoostBelowLevelEight() {
        Permanent dragonlord = addCreatureReady(player1, new KarganDragonlord());
        for (int level : new int[]{0, 4, 7}) {
            dragonlord.setCounterCount(CounterType.LEVEL, level);
            prepareForLeveling(player1);
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("Level up cannot be activated outside a main phase")
    void levelUpRequiresMainPhase() {
        addCreatureReady(player1, new KarganDragonlord());
        prepareForLeveling(player1);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Level up cannot be activated during an opponent's turn")
    void levelUpRequiresOwnTurn() {
        addCreatureReady(player1, new KarganDragonlord());
        prepareForLeveling(player1);
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Level up uses the stack and cannot be activated with another ability pending")
    void levelUpRequiresEmptyStack() {
        Permanent dragonlord = addCreatureReady(player1, new KarganDragonlord());
        prepareForLeveling(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(dragonlord.getCounterCount(CounterType.LEVEL)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(dragonlord.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
    }

    @Test
    @DisplayName("At level eight the power boost works at instant speed and multiple boosts accumulate")
    void boostWorksDuringOpponentsUpkeep() {
        Permanent dragonlord = addCreatureReady(player1, new KarganDragonlord());
        dragonlord.setCounterCount(CounterType.LEVEL, 8);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertStats(dragonlord, 10, 8);
    }

    @Test
    @DisplayName("A power boost already on the stack resolves even if the level counters are removed")
    void pendingBoostSurvivesLossOfLevels() {
        Permanent dragonlord = addCreatureReady(player1, new KarganDragonlord());
        dragonlord.setCounterCount(CounterType.LEVEL, 8);
        prepareForLeveling(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        dragonlord.setCounterCount(CounterType.LEVEL, 0);
        harness.passBothPriorities();

        assertStats(dragonlord, 3, 2);
    }

    @Test
    @DisplayName("Level up remains available above level eight")
    void canLevelUpBeyondHighestThreshold() {
        Permanent dragonlord = addCreatureReady(player1, new KarganDragonlord());
        dragonlord.setCounterCount(CounterType.LEVEL, 9);
        levelUp(player1);

        assertThat(dragonlord.getCounterCount(CounterType.LEVEL)).isEqualTo(10);
        assertStats(dragonlord, 8, 8);
        assertThat(gqs.hasKeyword(gd, dragonlord, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dragonlord, Keyword.TRAMPLE)).isTrue();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player, ManaColor.RED, 1);
    }

    private void levelUp(Player player) {
        prepareForLeveling(player);
        harness.activateAbility(player, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
