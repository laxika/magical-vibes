package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BrimstoneMage.class})
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

    @Test
    void levelCounterIsAddedOnlyOnResolutionAndCannotLevelWithNonemptyStack() {
        Permanent mage = addCreatureReady(player1, new BrimstoneMage());
        prepareForLeveling(player1, 8);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mage.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(mage.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(mage, 2, 3);
    }

    @Test
    void cannotLevelOutsideMainPhaseOrDuringOpponentsTurn() {
        Permanent mage = addCreatureReady(player1, new BrimstoneMage());
        prepareForLeveling(player1, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mage.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canLevelWhileTappedAndSummoningSickInPostcombatMainPhase() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new BrimstoneMage());
        mage.setTapped(true);
        mage.setSummoningSick(true);
        prepareForLeveling(player1, 4);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(mage.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(mage.isTapped()).isTrue();
        assertStats(mage, 2, 3);
    }

    @Test
    void cannotUseDamageAbilityWithoutLevelCountersOrWhileSummoningSick() {
        Permanent mage = addCreatureReady(player1, new BrimstoneMage());
        prepareForLeveling(player1, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        levelUp(player1);
        mage.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mage.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void levelTwoAbilityCanDamageACreatureAndCannotBeActivatedAgainWhileTapped() {
        Permanent mage = addCreatureReady(player1, new BrimstoneMage());
        Permanent target = addCreatureReady(player2, new BrimstoneMage());
        levelUp(player1);
        levelUp(player1);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    void aboveLevelThreeAbilityDealsLethalDamageToACreature() {
        Permanent mage = addCreatureReady(player1, new BrimstoneMage());
        Permanent target = addCreatureReady(player2, new BrimstoneMage());
        mage.setCounterCount(CounterType.LEVEL, 4);
        prepareForLeveling(player1, 0);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Brimstone Mage");
        assertStats(mage, 2, 4);
    }

    @Test
    void damageAmountRemainsOneWhenLevelChangesAfterActivation() {
        Permanent mage = addCreatureReady(player1, new BrimstoneMage());
        levelUp(player1);
        levelUp(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        mage.setCounterCount(CounterType.LEVEL, 3);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertStats(mage, 2, 4);
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
