package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SkywatcherAdept.class})
class SkywatcherAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Flying applies only while Skywatcher Adept has at least one level counter")
    void flyingFollowsLevelCounters() {
        Permanent adept = addCreatureReady(player1, new SkywatcherAdept());
        assertThat(gqs.hasKeyword(gd, adept, Keyword.FLYING)).isFalse();

        prepareForLeveling(player1);
        levelUp(player1);
        assertThat(gqs.hasKeyword(gd, adept, Keyword.FLYING)).isTrue();

        levelUp(player1);
        assertThat(gqs.hasKeyword(gd, adept, Keyword.FLYING)).isTrue();

        levelUp(player1);
        assertThat(gqs.hasKeyword(gd, adept, Keyword.FLYING)).isTrue();

        adept.setCounterCount(CounterType.LEVEL, 0);
        assertThat(gqs.hasKeyword(gd, adept, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Level up pays three mana and adds its counter only on resolution, even while summoning sick")
    void levelUpUsesStackAndDoesNotRequireTap() {
        Permanent adept = addCreatureReady(player1, new SkywatcherAdept());
        adept.setSummoningSick(true);
        adept.setTapped(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(adept.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();
        assertThat(adept.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(adept, 2, 2);
        assertThat(adept.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Level up cannot be activated while another level up is on the stack")
    void levelUpRequiresEmptyStack() {
        Permanent adept = addCreatureReady(player1, new SkywatcherAdept());
        prepareForLeveling(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(gd.stack).hasSize(1);
        assertThat(adept.getCounterCount(CounterType.LEVEL)).isZero();
        harness.passBothPriorities();
        assertThat(adept.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Level up cannot be activated during an opponent's main phase")
    void levelUpRequiresControllersTurn() {
        Permanent adept = addCreatureReady(player1, new SkywatcherAdept());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(adept.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    @DisplayName("Power and toughness follow the current level when counters are removed or exceed level three")
    void statsFollowCurrentLevel() {
        Permanent adept = addCreatureReady(player1, new SkywatcherAdept());
        adept.setCounterCount(CounterType.LEVEL, 4);
        assertStats(adept, 4, 2);

        adept.setCounterCount(CounterType.LEVEL, 2);
        assertStats(adept, 2, 2);

        adept.setCounterCount(CounterType.LEVEL, 0);
        assertStats(adept, 1, 1);
    }

    @Test
    @DisplayName("Leveling up changes Skywatcher Adept's base power and toughness at its thresholds")
    void levelsUpAtThresholds() {
        Permanent adept = addCreatureReady(player1, new SkywatcherAdept());

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(adept.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(adept, 2, 2);

        levelUp(player1);

        assertThat(adept.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
        assertStats(adept, 2, 2);

        levelUp(player1);

        assertThat(adept.getCounterCount(CounterType.LEVEL)).isEqualTo(3);
        assertStats(adept, 4, 2);
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent adept = addCreatureReady(player1, new SkywatcherAdept());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> levelUp(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(adept.getCounterCount(CounterType.LEVEL)).isZero();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 9);
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
