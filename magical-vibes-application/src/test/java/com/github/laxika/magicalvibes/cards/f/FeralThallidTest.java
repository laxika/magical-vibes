package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(FeralThallid.class)
class FeralThallidTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent thallid = addThallid();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing three spore counters grants a regeneration shield")
    void removesThreeSporeCountersAndRegenerates() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(thallid.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing three spore counters leaves any additional spore counters")
    void removesExactlyThreeSporeCounters() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
        assertThat(thallid.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability requires three spore counters")
    void regenerationAbilityRequiresThreeSporeCounters() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addThallid() {
        return addCreatureReady(player1, new FeralThallid());
    }

    @Test
    void onlyActivePlayersThallidGetsUpkeepCounter() {
        Permanent own = addThallid();
        Permanent opposing = addCreatureReady(player2, new FeralThallid());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(own.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(opposing.getCounterCount(CounterType.FUNGUS)).isEqualTo(1);
    }

    @Test
    void countersArePaidBeforeRegenerationResolves() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(thallid.getRegenerationShield()).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(thallid.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void tappedSummoningSickThallidCanRegenerate() {
        Permanent thallid = harness.addToBattlefieldAndReturn(player1, new FeralThallid());
        thallid.setSummoningSick(true);
        thallid.tap();
        thallid.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thallid.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(thallid.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationPreventsLethalDamageDestructionOnce() {
        Permanent thallid = addThallid();
        thallid.setCounterCount(CounterType.FUNGUS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        thallid.setAttacking(true);
        thallid.setMarkedDamage(3);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(thallid);
        assertThat(thallid.isTapped()).isTrue();
        assertThat(thallid.isAttacking()).isFalse();
        assertThat(thallid.getMarkedDamage()).isZero();
        assertThat(thallid.getRegenerationShield()).isZero();

        thallid.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Feral Thallid");
        harness.assertInGraveyard(player1, "Feral Thallid");
    }
}
