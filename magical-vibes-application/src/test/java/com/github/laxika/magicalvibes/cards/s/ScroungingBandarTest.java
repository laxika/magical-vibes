package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScroungingBandar.class, DruidOfTheCowl.class})
class ScroungingBandarTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        Permanent bandar = castBandar();

        assertThat(bandar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Upkeep may move a chosen number of +1/+1 counters onto another creature")
    void upkeepMovesChosenNumberOfCounters() {
        Permanent bandar = castBandar();
        Permanent target = addCreatureReady(player1, new DruidOfTheCowl());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "1");

        assertThat(bandar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep ability leaves the counters in place")
    void decliningLeavesCountersInPlace() {
        Permanent bandar = castBandar();
        Permanent target = addCreatureReady(player1, new DruidOfTheCowl());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bandar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The upkeep ability leaves no stack entry when there is no legal target")
    void leavesNoStackEntryWithoutLegalTarget() {
        Permanent bandar = castBandar();

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(bandar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void mayChooseZeroCounters() {
        Permanent bandar = castBandar();
        Permanent target = addCreatureReady(player1, new DruidOfTheCowl());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "0");

        assertThat(bandar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Scrounging Bandar");
    }

    @Test
    void movingAllCountersCausesBandarToDie() {
        castBandar();
        Permanent target = addCreatureReady(player1, new DruidOfTheCowl());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "2");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Scrounging Bandar");
        harness.assertInGraveyard(player1, "Scrounging Bandar");
    }

    @Test
    void canMoveCountersOntoOpponentsCreature() {
        Permanent bandar = castBandar();
        Permanent target = addCreatureReady(player2, new DruidOfTheCowl());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "1");

        assertThat(bandar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetItself() {
        Permanent bandar = castBandar();
        Permanent target = addCreatureReady(player1, new DruidOfTheCowl());

        advanceToUpkeep(player1);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bandar.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bandar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent bandar = castBandar();
        Permanent target = addCreatureReady(player1, new DruidOfTheCowl());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bandar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castBandar() {
        harness.castFromHand(player1, new ScroungingBandar(), "{1}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Scrounging Bandar");
    }
}
