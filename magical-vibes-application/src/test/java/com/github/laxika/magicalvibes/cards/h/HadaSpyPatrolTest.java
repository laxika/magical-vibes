package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HadaSpyPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Hada Spy Patrol's stats and abilities at each threshold")
    void levelsUpAtThresholds() {
        Permanent patrol = addCreatureReady(player1, new HadaSpyPatrol());

        assertStats(patrol, 1, 1);
        assertThat(gqs.hasCantBeBlocked(gd, patrol)).isFalse();
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.SHROUD)).isFalse();

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(patrol.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(patrol, 2, 2);
        assertThat(gqs.hasCantBeBlocked(gd, patrol)).isTrue();
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.SHROUD)).isFalse();

        levelUp(player1);
        assertStats(patrol, 2, 2);

        levelUp(player1);
        assertThat(patrol.getCounterCount(CounterType.LEVEL)).isEqualTo(3);
        assertStats(patrol, 3, 3);
        assertThat(gqs.hasCantBeBlocked(gd, patrol)).isTrue();
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Level three Hada Spy Patrol cannot be targeted because it has shroud")
    void levelThreeHasShroud() {
        Permanent patrol = addCreatureReady(player1, new HadaSpyPatrol());
        prepareForLeveling(player1);
        levelUp(player1);
        levelUp(player1);
        levelUp(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, patrol.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent patrol = addCreatureReady(player1, new HadaSpyPatrol());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> levelUp(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(patrol.getCounterCount(CounterType.LEVEL)).isZero();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.BLUE, 3);
        harness.addMana(player, ManaColor.COLORLESS, 6);
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
