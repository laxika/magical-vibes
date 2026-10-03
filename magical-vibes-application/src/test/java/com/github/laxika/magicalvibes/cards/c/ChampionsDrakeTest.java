package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KeeningStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChampionsDrake.class, CaravanEscort.class, KeeningStone.class})
class ChampionsDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+3 when you control a creature with at least three level counters")
    void getsBoostWithThreeLevelCounters() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new ChampionsDrake());
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());

        assertStats(drake, 1, 1);

        levelUpThreeTimes(player1, escort);

        assertStats(drake, 4, 4);
    }

    @Test
    @DisplayName("Does not get the boost with fewer than three level counters")
    void doesNotBoostBelowThreeLevelCounters() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new ChampionsDrake());
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());

        levelUp(player1, escort, 2);

        assertStats(drake, 1, 1);
    }

    @Test
    @DisplayName("Does not count a creature controlled by an opponent")
    void opponentCreatureDoesNotCount() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new ChampionsDrake());
        Permanent escort = harness.addToBattlefieldAndReturn(player2, new CaravanEscort());

        levelUpThreeTimes(player2, escort);

        assertStats(drake, 1, 1);
    }

    @Test
    void countersOnDifferentCreaturesDoNotCombine() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new ChampionsDrake());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        first.setCounterCount(CounterType.LEVEL, 2);
        second.setCounterCount(CounterType.LEVEL, 2);

        assertStats(drake, 1, 1);
    }

    @Test
    void multipleQualifyingCreaturesGiveOnlyOneBoost() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new ChampionsDrake());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        first.setCounterCount(CounterType.LEVEL, 3);
        second.setCounterCount(CounterType.LEVEL, 5);

        assertStats(drake, 4, 4);
        first.setCounterCount(CounterType.LEVEL, 2);
        assertStats(drake, 4, 4);
        second.setCounterCount(CounterType.LEVEL, 2);
        assertStats(drake, 1, 1);
    }

    @Test
    void losesBoostWhenQualifyingCreatureLeaves() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new ChampionsDrake());
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        levelUpThreeTimes(player1, escort);
        assertStats(drake, 4, 4);

        gd.playerBattlefields.get(player1.getId()).remove(escort);

        assertStats(drake, 1, 1);
    }

    @Test
    void levelCountersOnNoncreatureDoNotQualify() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new ChampionsDrake());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new KeeningStone());
        artifact.setCounterCount(CounterType.LEVEL, 3);

        assertStats(drake, 1, 1);
    }

    @Test
    void otherCounterTypesDoNotQualify() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new ChampionsDrake());
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        escort.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertStats(drake, 1, 1);
    }

    @Test
    void drakeItselfCanBeTheQualifyingCreature() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new ChampionsDrake());
        drake.setCounterCount(CounterType.LEVEL, 3);

        assertStats(drake, 4, 4);
    }

    private void levelUpThreeTimes(Player player, Permanent escort) {
        levelUp(player, escort, 3);
    }

    private void levelUp(Player player, Permanent escort, int times) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, times * 2);

        int permanentIndex = gd.playerBattlefields.get(player.getId()).indexOf(escort);
        for (int i = 0; i < times; i++) {
            harness.activateAbility(player, permanentIndex, 0, null, null);
            harness.passBothPriorities();
        }
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
