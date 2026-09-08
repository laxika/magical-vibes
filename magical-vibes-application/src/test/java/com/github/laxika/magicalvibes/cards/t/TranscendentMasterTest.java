package com.github.laxika.magicalvibes.cards.t;

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

class TranscendentMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Transcendent Master's stats and grants indestructible at level 12")
    void levelsUpAtThresholds() {
        Permanent master = addCreatureReady(player1, new TranscendentMaster());

        assertStats(master, 3, 3);
        assertThat(gqs.hasKeyword(gd, master, Keyword.INDESTRUCTIBLE)).isFalse();

        prepareForLeveling(player1);
        for (int i = 0; i < 6; i++) {
            levelUp(player1);
        }

        assertThat(master.getCounterCount(CounterType.LEVEL)).isEqualTo(6);
        assertStats(master, 6, 6);
        assertThat(gqs.hasKeyword(gd, master, Keyword.INDESTRUCTIBLE)).isFalse();

        for (int i = 0; i < 6; i++) {
            levelUp(player1);
        }

        assertThat(master.getCounterCount(CounterType.LEVEL)).isEqualTo(12);
        assertStats(master, 9, 9);
        assertThat(gqs.hasKeyword(gd, master, Keyword.INDESTRUCTIBLE)).isTrue();
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
