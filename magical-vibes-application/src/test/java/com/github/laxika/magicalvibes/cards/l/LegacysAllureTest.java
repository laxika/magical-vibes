package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BenthicBehemoth;
import com.github.laxika.magicalvibes.cards.m.MoggConscripts;
import com.github.laxika.magicalvibes.cards.w.WallOfDiffusion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LegacysAllure.class, MoggConscripts.class, BenthicBehemoth.class, WallOfDiffusion.class})
class LegacysAllureTest extends BaseCardTest {

    private Permanent addAllureWithCounters(int treasureCounters) {
        Permanent allure = harness.addToBattlefieldAndReturn(player1, new LegacysAllure());
        allure.setCounterCount(CounterType.TREASURE, treasureCounters);
        return allure;
    }

    @Test
    @DisplayName("Accepting the upkeep trigger puts a treasure counter on it")
    void upkeepAcceptedAddsCounter() {
        Permanent allure = addAllureWithCounters(0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(allure.getCounterCount(CounterType.TREASURE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves the counters unchanged")
    void upkeepDeclinedAddsNoCounter() {
        Permanent allure = addAllureWithCounters(0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(allure.getCounterCount(CounterType.TREASURE)).isZero();
    }

    @Test
    @DisplayName("Sacrificing with two treasure counters steals a 2/2 permanently")
    void stealsCreatureWithinCounterCount() {
        addAllureWithCounters(2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoggConscripts());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mogg Conscripts");
        harness.assertNotOnBattlefield(player2, "Mogg Conscripts");
        harness.assertNotOnBattlefield(player1, "Legacy's Allure");
    }

    @Test
    @DisplayName("Cannot target a creature with power above the treasure counter count")
    void cannotTargetTooLargeCreature() {
        addAllureWithCounters(1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenthicBehemoth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with power");
    }

    @Test
    @DisplayName("With no treasure counters even a larger creature stays untouchable")
    void noCountersMeansNoLegalTarget() {
        addAllureWithCounters(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenthicBehemoth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with power");
    }

    @Test
    @DisplayName("With no treasure counters a zero-power creature is a legal target")
    void zeroPowerCreatureIsLegalWithNoCounters() {
        addAllureWithCounters(0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfDiffusion());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wall of Diffusion");
        harness.assertNotOnBattlefield(player2, "Wall of Diffusion");
    }

    @Test
    @DisplayName("A target that becomes too powerful before resolution is not controlled")
    void targetBecomingTooPowerfulBeforeResolutionIsNotControlled() {
        addAllureWithCounters(2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoggConscripts());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mogg Conscripts");
        harness.assertNotOnBattlefield(player1, "Mogg Conscripts");
    }
}
