package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScarlandThrinax.class, CanyonMinotaur.class})
class ScarlandThrinaxTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on Scarland Thrinax")
    void sacrificeCreatureAddsCounter() {
        Permanent thrinax = addCreatureReady(player1, new ScarlandThrinax());
        Permanent minotaur = addCreatureReady(player1, new CanyonMinotaur());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, minotaur.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Canyon Minotaur");
        harness.assertInGraveyard(player1, "Canyon Minotaur");
        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated sacrifices accumulate +1/+1 counters")
    void multipleActivationsAccumulateCounters() {
        Permanent thrinax = addCreatureReady(player1, new ScarlandThrinax());
        Permanent minotaur1 = addCreatureReady(player1, new CanyonMinotaur());
        Permanent minotaur2 = addCreatureReady(player1, new CanyonMinotaur());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, minotaur1.getId());
        harness.passBothPriorities();
        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, minotaur2.getId());
        harness.passBothPriorities();
        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Scarland Thrinax may sacrifice itself (cost is 'a creature', not 'another')")
    void canSacrificeItself() {
        addCreatureReady(player1, new ScarlandThrinax());

        // Only creature on the battlefield — the cost auto-selects Scarland itself.
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scarland Thrinax");
        harness.assertInGraveyard(player1, "Scarland Thrinax");
    }

    @Test
    @DisplayName("The creature is sacrificed as a cost before the counter is added")
    void sacrificeIsPaidBeforeResolution() {
        Permanent thrinax = addCreatureReady(player1, new ScarlandThrinax());
        Permanent minotaur = addCreatureReady(player1, new CanyonMinotaur());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, minotaur.getId());

        harness.assertNotOnBattlefield(player1, "Canyon Minotaur");
        harness.assertInGraveyard(player1, "Canyon Minotaur");
        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent thrinax = harness.addToBattlefieldAndReturn(player1, new ScarlandThrinax());
        thrinax.setSummoningSick(true);
        thrinax.setTapped(true);
        Permanent minotaur = addCreatureReady(player1, new CanyonMinotaur());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, minotaur.getId());
        harness.passBothPriorities();

        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Canyon Minotaur");
    }

    @Test
    @DisplayName("Sacrificing the source in response leaves both abilities with no source to put counters on")
    void sacrificingSourceInResponseDoesNotAddCounters() {
        Permanent thrinax = addCreatureReady(player1, new ScarlandThrinax());
        Permanent minotaur = addCreatureReady(player1, new CanyonMinotaur());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, minotaur.getId());
        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Scarland Thrinax");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scarland Thrinax");
        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
