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
        assertThat(gqs.hasKeyword(gd, dragonlord, Keyword.TRAMPLE)).isFalse();

        for (int i = 0; i < 4; i++) {
            levelUp(player1);
        }

        assertThat(dragonlord.getCounterCount(CounterType.LEVEL)).isEqualTo(8);
        assertStats(dragonlord, 8, 8);
        assertThat(gqs.hasKeyword(gd, dragonlord, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Kargan Dragonlord can boost its power until end of turn")
    void boostsPowerUntilEndOfTurn() {
        Permanent dragonlord = addCreatureReady(player1, new KarganDragonlord());
        prepareForLeveling(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertStats(dragonlord, 3, 2);
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
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
