package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AfiyaGrove.class, BayFalcon.class})
class AfiyaGroveTest extends BaseCardTest {

    private Permanent castGrove(Player player) {
        harness.castFromHand(player, new AfiyaGrove(), "{1}{G}");
        harness.passBothPriorities();
        return findPermanent(player, "Afiya Grove");
    }

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        Permanent grove = castGrove(player1);

        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Upkeep trigger moves a +1/+1 counter onto the chosen creature")
    void upkeepTriggerMovesCounter() {
        Permanent grove = castGrove(player1);
        Permanent falcon = addCreatureReady(player1, new BayFalcon());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, falcon.getId());
        resolveAllTriggers();

        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(falcon.getEffectivePower()).isEqualTo(2);
        assertThat(falcon.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The counter can be moved onto an opponent's creature")
    void canMoveCounterOntoOpponentCreature() {
        Permanent grove = castGrove(player1);
        Permanent falcon = addCreatureReady(player2, new BayFalcon());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, falcon.getId());
        resolveAllTriggers();

        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent grove = castGrove(player1);
        Permanent falcon = addCreatureReady(player1, new BayFalcon());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Trigger does nothing with no creature on the battlefield")
    void noCreatureMeansNoTrigger() {
        Permanent grove = castGrove(player1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificed once its last +1/+1 counter is moved off")
    void sacrificedWhenLastCounterLeaves() {
        Permanent grove = castGrove(player1);
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        grove.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, falcon.getId());
        resolveAllTriggers();

        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Afiya Grove")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> "Afiya Grove".equals(c.getName()));
    }

    @Test
    @DisplayName("Sacrifices when it has no +1/+1 counters")
    void sacrificesWhenAlreadyEmpty() {
        castGrove(player1).setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Afiya Grove")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> "Afiya Grove".equals(c.getName()));
    }
}
