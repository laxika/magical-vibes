package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RunawaySteamKinTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a red spell puts a +1/+1 counter on Runaway Steam-Kin")
    void redSpellAddsCounter() {
        Permanent steamKin = addSteamKin();
        harness.setHand(player1, List.of(new RagingGoblin()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(steamKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The red-spell trigger does not put a fourth counter on Runaway Steam-Kin")
    void redSpellDoesNotAddCounterAtThree() {
        Permanent steamKin = addSteamKin();
        steamKin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new RagingGoblin()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(steamKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The red-spell trigger checks its counter condition again on resolution")
    void redSpellTriggerDoesNotResolveAfterCounterThresholdIsReached() {
        Permanent steamKin = addSteamKin();
        steamKin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new RagingGoblin()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        steamKin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        assertThat(steamKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a nonred spell does not trigger Runaway Steam-Kin")
    void nonRedSpellDoesNotAddCounter() {
        Permanent steamKin = addSteamKin();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(steamKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Removing three +1/+1 counters adds three red mana")
    void removeThreeCountersAddsThreeRedMana() {
        Permanent steamKin = addSteamKin();
        steamKin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(steamKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    private Permanent addSteamKin() {
        return harness.addToBattlefieldAndReturn(player1, new RunawaySteamKin());
    }
}
