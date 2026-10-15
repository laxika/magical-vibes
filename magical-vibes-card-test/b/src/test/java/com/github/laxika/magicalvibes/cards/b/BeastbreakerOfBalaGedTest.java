package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BeastbreakerOfBalaGed.class})
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

    @Test
    void levelCounterIsAddedOnResolutionAndCannotLevelWithNonemptyStack() {
        Permanent beastbreaker = addCreatureReady(player1, new BeastbreakerOfBalaGed());
        prepareForLeveling(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(beastbreaker.getCounterCount(CounterType.LEVEL)).isZero();
        assertStats(beastbreaker, 2, 2);
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(beastbreaker.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(beastbreaker, 4, 4);
    }

    @Test
    void cannotLevelOutsideMainPhase() {
        Permanent beastbreaker = addCreatureReady(player1, new BeastbreakerOfBalaGed());
        prepareForLeveling(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(beastbreaker.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotLevelDuringOpponentsTurn() {
        Permanent beastbreaker = addCreatureReady(player1, new BeastbreakerOfBalaGed());
        prepareForLeveling(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(beastbreaker.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canLevelWhileTappedAndSummoningSickInPostcombatMainPhase() {
        Permanent beastbreaker = harness.addToBattlefieldAndReturn(player1, new BeastbreakerOfBalaGed());
        beastbreaker.setSummoningSick(true);
        beastbreaker.tap();
        prepareForLeveling(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        levelUp(player1);

        assertThat(beastbreaker.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(beastbreaker, 4, 4);
        assertThat(beastbreaker.isTapped()).isTrue();
    }

    @Test
    void levelRangesUpdateWhenCountersDecreaseAndOtherCountersStillModifyStats() {
        Permanent beastbreaker = addCreatureReady(player1, new BeastbreakerOfBalaGed());
        beastbreaker.setCounterCount(CounterType.LEVEL, 5);
        beastbreaker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertStats(beastbreaker, 7, 7);
        assertThat(gqs.hasKeyword(gd, beastbreaker, Keyword.TRAMPLE)).isTrue();

        beastbreaker.setCounterCount(CounterType.LEVEL, 3);

        assertStats(beastbreaker, 5, 5);
        assertThat(gqs.hasKeyword(gd, beastbreaker, Keyword.TRAMPLE)).isFalse();

        beastbreaker.setCounterCount(CounterType.LEVEL, 0);

        assertStats(beastbreaker, 3, 3);
        assertThat(gqs.hasKeyword(gd, beastbreaker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotLevelWithoutGreenMana() {
        Permanent beastbreaker = addCreatureReady(player1, new BeastbreakerOfBalaGed());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(beastbreaker.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gd.stack).isEmpty();
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
