package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KabiraVindicator.class, NestInvader.class})
class KabiraVindicatorTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Kabira Vindicator and its other creatures anthem")
    void levelsUpAtThresholds() {
        Permanent vindicator = addCreatureReady(player1, new KabiraVindicator());
        Permanent ownBear = addCreatureReady(player1, new NestInvader());
        Permanent opposingBear = addCreatureReady(player2, new NestInvader());

        assertStats(vindicator, 2, 4);
        assertStats(ownBear, 2, 2);
        assertStats(opposingBear, 2, 2);

        prepareForLeveling(player1);
        levelUp(player1, vindicator);

        assertThat(vindicator.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(vindicator, 2, 4);
        assertStats(ownBear, 2, 2);

        levelUp(player1, vindicator);

        assertThat(vindicator.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
        assertStats(vindicator, 3, 6);
        assertStats(ownBear, 3, 3);
        assertStats(opposingBear, 2, 2);

        for (int i = 0; i < 3; i++) {
            levelUp(player1, vindicator);
        }

        assertThat(vindicator.getCounterCount(CounterType.LEVEL)).isEqualTo(5);
        assertStats(vindicator, 4, 8);
        assertStats(ownBear, 4, 4);
        assertStats(opposingBear, 2, 2);
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent vindicator = addCreatureReady(player1, new KabiraVindicator());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> levelUp(player1, vindicator))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(vindicator.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    void levelFourUsesMiddleBonusAndLosingLevelsRemovesIt() {
        Permanent vindicator = addCreatureReady(player1, new KabiraVindicator());
        Permanent ally = addCreatureReady(player1, new NestInvader());
        vindicator.setCounterCount(CounterType.LEVEL, 4);

        assertStats(vindicator, 3, 6);
        assertStats(ally, 3, 3);

        vindicator.setCounterCount(CounterType.LEVEL, 6);
        assertStats(vindicator, 4, 8);
        assertStats(ally, 4, 4);

        vindicator.setCounterCount(CounterType.LEVEL, 1);
        assertStats(vindicator, 2, 4);
        assertStats(ally, 2, 2);
    }

    @Test
    void multipleVindicatorsBoostEachOtherAndTheirBonusesStack() {
        Permanent middleLevel = addCreatureReady(player1, new KabiraVindicator());
        Permanent highLevel = addCreatureReady(player1, new KabiraVindicator());
        Permanent ally = addCreatureReady(player1, new NestInvader());
        middleLevel.setCounterCount(CounterType.LEVEL, 2);
        highLevel.setCounterCount(CounterType.LEVEL, 5);

        assertStats(middleLevel, 5, 8);
        assertStats(highLevel, 5, 9);
        assertStats(ally, 5, 5);
    }

    @Test
    void levelUpDoesNotRequireTappingOrHaste() {
        Permanent vindicator = harness.addToBattlefieldAndReturn(player1, new KabiraVindicator());
        vindicator.setSummoningSick(true);
        vindicator.tap();
        prepareForLeveling(player1);

        levelUp(player1, vindicator);

        assertThat(vindicator.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(vindicator.isTapped()).isTrue();
    }

    @Test
    void levelCounterIsAddedOnResolutionAndCannotLevelAgainWithStackOccupied() {
        Permanent vindicator = addCreatureReady(player1, new KabiraVindicator());
        prepareForLeveling(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(vindicator.getCounterCount(CounterType.LEVEL)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        assertThat(vindicator.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
    }

    @Test
    void levelUpCannotBeActivatedDuringOpponentsMainPhase() {
        Permanent vindicator = addCreatureReady(player1, new KabiraVindicator());
        prepareForLeveling(player1);
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> levelUp(player1, vindicator))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(vindicator.getCounterCount(CounterType.LEVEL)).isZero();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.WHITE, 5);
        harness.addMana(player, ManaColor.COLORLESS, 10);
    }

    private void levelUp(Player player, Permanent vindicator) {
        int permanentIndex = gd.playerBattlefields.get(player.getId()).indexOf(vindicator);
        harness.activateAbility(player, permanentIndex, 0, null, null);
        harness.passBothPriorities();
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
