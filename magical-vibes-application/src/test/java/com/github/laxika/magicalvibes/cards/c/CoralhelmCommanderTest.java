package com.github.laxika.magicalvibes.cards.c;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoralhelmCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Coralhelm Commander's stats and grants flying")
    void levelsUpAtThresholds() {
        Permanent commander = addCreatureReady(player1, new CoralhelmCommander());

        assertStats(commander, 2, 2);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.FLYING)).isFalse();

        prepareForLeveling(player1);
        levelUp(player1);
        levelUp(player1);

        assertThat(commander.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
        assertStats(commander, 3, 3);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.FLYING)).isTrue();

        levelUp(player1);
        levelUp(player1);

        assertThat(commander.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
        assertStats(commander, 4, 4);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("At level four Coralhelm Commander boosts other Merfolk you control")
    void boostsOtherMerfolkAtLevelFour() {
        Permanent commander = addCreatureReady(player1, new CoralhelmCommander());
        Permanent allyMerfolk = addCreatureReady(player1, new CoralhelmCommander());
        Permanent nonMerfolk = addCreatureReady(player1, new CaravanEscort());
        Permanent opponentMerfolk = addCreatureReady(player2, new CoralhelmCommander());

        prepareForLeveling(player1);
        for (int i = 0; i < 4; i++) {
            levelUp(player1);
        }

        assertStats(commander, 4, 4);
        assertStats(allyMerfolk, 3, 3);
        assertStats(nonMerfolk, 1, 1);
        assertStats(opponentMerfolk, 2, 2);
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent commander = addCreatureReady(player1, new CoralhelmCommander());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> levelUp(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(commander.getCounterCount(CounterType.LEVEL)).isZero();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 4);
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
