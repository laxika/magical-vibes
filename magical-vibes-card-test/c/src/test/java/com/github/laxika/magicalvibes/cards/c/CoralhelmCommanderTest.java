package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Snakeform;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoralhelmCommander.class, CaravanEscort.class, Snakeform.class})
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

    @Test
    @DisplayName("Level bonuses disappear when level counters are removed")
    void losingCountersUpdatesAbilitiesAndAnthem() {
        Permanent commander = addCreatureReady(player1, new CoralhelmCommander());
        Permanent ally = addCreatureReady(player1, new CoralhelmCommander());
        commander.setCounterCount(CounterType.LEVEL, 5);

        assertStats(commander, 4, 4);
        assertStats(ally, 3, 3);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.FLYING)).isTrue();

        commander.setCounterCount(CounterType.LEVEL, 3);
        assertStats(commander, 3, 3);
        assertStats(ally, 2, 2);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.FLYING)).isTrue();

        commander.setCounterCount(CounterType.LEVEL, 1);
        assertStats(commander, 2, 2);
        assertStats(ally, 2, 2);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Multiple level-four commanders boost each other and bonuses stack")
    void multipleCommandersBoostEachOther() {
        Permanent first = addCreatureReady(player1, new CoralhelmCommander());
        Permanent second = addCreatureReady(player1, new CoralhelmCommander());
        Permanent ally = addCreatureReady(player1, new CoralhelmCommander());
        first.setCounterCount(CounterType.LEVEL, 4);
        second.setCounterCount(CounterType.LEVEL, 4);

        assertStats(first, 5, 5);
        assertStats(second, 5, 5);
        assertStats(ally, 4, 4);
    }

    @Test
    @DisplayName("Level up works while summoning sick and adds its counter only on resolution")
    void levelUpWhileSummoningSick() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new CoralhelmCommander());
        commander.setSummoningSick(true);
        prepareForLeveling(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(commander.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(commander.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(commander, 2, 2);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Level up cannot be activated with another level up on the stack")
    void levelUpRequiresEmptyStack() {
        Permanent commander = addCreatureReady(player1, new CoralhelmCommander());
        prepareForLeveling(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        assertThat(commander.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Plus-one counters modify the level-defined base stats without granting levels")
    void otherCountersDoNotCountAsLevels() {
        Permanent commander = addCreatureReady(player1, new CoralhelmCommander());
        commander.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertStats(commander, 4, 4);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.FLYING)).isFalse();

        commander.setCounterCount(CounterType.LEVEL, 2);
        assertStats(commander, 5, 5);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.FLYING)).isTrue();
    }

    @Test
    @CardUsed({CoralhelmCommander.class, Snakeform.class})
    @DisplayName("Losing all abilities removes flying granted by the level symbol")
    void snakeformRemovesLevelGrantedFlying() {
        Permanent commander = addCreatureReady(player1, new CoralhelmCommander());
        commander.setCounterCount(CounterType.LEVEL, 4);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.FLYING)).isTrue();
        harness.setHand(player1, List.of(new Snakeform()));
        harness.setLibrary(player1, List.of(new CoralhelmCommander()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, commander.getId());

        assertStats(commander, 1, 1);
        assertThat(commander.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.FLYING)).isFalse();
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
