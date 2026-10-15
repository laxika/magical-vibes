package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.c.Crocanura;
import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GyreSage.class, Crocanura.class, DiscipleOfTheOldWays.class,
        BurstOfStrength.class})
class GyreSageTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping with no +1/+1 counters produces no mana")
    void tapWithoutCountersProducesNoMana() {
        addCreatureReady(player1, new GyreSage());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Tapping produces one green mana per +1/+1 counter")
    void tapProducesGreenManaPerCounter() {
        Permanent sage = addCreatureReady(player1, new GyreSage());
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Evolve grows Gyre Sage and its mana ability scales with the counter")
    void evolveGrowsSageAndItsManaAbility() {
        Permanent sage = addCreatureReady(player1, new GyreSage());

        // Disciple of the Old Ways is 2/2, with greater power than Gyre Sage.
        harness.setHand(player1, List.of(new DiscipleOfTheOldWays()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void manaAbilityResolvesImmediatelyWithoutConsumingCounters() {
        Permanent sage = addCreatureReady(player1, new GyreSage());
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sage.isTapped()).isTrue();
        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void evolvesForGreaterToughnessOnly() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new GyreSage());
        harness.setHand(player1, List.of(new Crocanura()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotEvolveForEqualStats() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new GyreSage());
        harness.setHand(player1, List.of(new GyreSage()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotEvolveForOpponentCreature() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new GyreSage());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DiscipleOfTheOldWays()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void evolveRechecksStatsAfterResponse() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new GyreSage());
        harness.setHand(player1, List.of(new DiscipleOfTheOldWays(), new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0, sage.getId());
        resolveAllTriggers();

        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTapWhileSummoningSick() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new GyreSage());
        sage.setSummoningSick(true);
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(sage.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        Permanent sage = addCreatureReady(player1, new GyreSage());
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
}
