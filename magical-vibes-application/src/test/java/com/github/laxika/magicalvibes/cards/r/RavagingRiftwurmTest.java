package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavagingRiftwurm.class})
class RavagingRiftwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two time counters without kicker")
    void entersWithTwoTimeCountersWithoutKicker() {
        harness.setHand(player1, List.of(new RavagingRiftwurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wurm = findPermanent(player1, "Ravaging Riftwurm");
        assertThat(wurm.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters with five time counters when kicked")
    void entersWithFiveTimeCountersWhenKicked() {
        harness.setHand(player1, List.of(new RavagingRiftwurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wurm = findPermanent(player1, "Ravaging Riftwurm");
        assertThat(wurm.getCounterCount(CounterType.TIME)).isEqualTo(5);
    }

    @Test
    @DisplayName("Removes one time counter during its controller's upkeep")
    void removesTimeCounterAtUpkeep() {
        Permanent wurm = addCreatureReady(player1, new RavagingRiftwurm());
        wurm.setCounterCount(CounterType.TIME, 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(wurm.getCounterCount(CounterType.TIME)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ravaging Riftwurm");
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed")
    void sacrificesWhenLastTimeCounterIsRemoved() {
        Permanent wurm = addCreatureReady(player1, new RavagingRiftwurm());
        wurm.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ravaging Riftwurm");
        harness.assertInGraveyard(player1, "Ravaging Riftwurm");
    }

    @Test
    @DisplayName("Does not sacrifice itself during upkeep when it has no time counters")
    void noTimeCountersDoesNotTriggerSacrifice() {
        Permanent wurm = addCreatureReady(player1, new RavagingRiftwurm());
        wurm.setCounterCount(CounterType.TIME, 0);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ravaging Riftwurm");
    }
}
