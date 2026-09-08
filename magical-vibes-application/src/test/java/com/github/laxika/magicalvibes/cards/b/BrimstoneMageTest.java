package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrimstoneMageTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Brimstone Mage's stats at both thresholds")
    void levelsUpAtThresholds() {
        Permanent mage = addCreatureReady(player1, new BrimstoneMage());
        prepareForLeveling(player1, 12);

        levelUp(player1);
        assertStats(mage, 2, 3);

        levelUp(player1);
        levelUp(player1);
        assertThat(mage.getCounterCount(CounterType.LEVEL)).isEqualTo(3);
        assertStats(mage, 2, 4);
    }

    @Test
    @DisplayName("At levels one through two Brimstone Mage deals 1 damage to any target")
    void dealsOneDamageAtLevelsOneThroughTwo() {
        Permanent mage = addCreatureReady(player1, new BrimstoneMage());
        levelUp(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At level three Brimstone Mage deals 3 damage to any target")
    void dealsThreeDamageAtLevelThree() {
        Permanent mage = addCreatureReady(player1, new BrimstoneMage());
        prepareForLeveling(player1, 12);
        levelUp(player1);
        levelUp(player1);
        levelUp(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(mage.isTapped()).isTrue();
    }

    private void prepareForLeveling(Player player, int redMana) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.RED, redMana);
    }

    private void levelUp(Player player) {
        prepareForLeveling(player, 4);
        harness.activateAbility(player, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
