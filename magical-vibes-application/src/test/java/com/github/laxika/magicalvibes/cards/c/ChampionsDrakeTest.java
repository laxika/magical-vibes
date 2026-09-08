package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
