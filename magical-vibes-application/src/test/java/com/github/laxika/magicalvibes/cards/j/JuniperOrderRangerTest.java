package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.r.RonomUnicorn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JuniperOrderRanger.class, RonomUnicorn.class})
class JuniperOrderRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on the entering creature and itself")
    void putsCountersOnAllyCreatureAndItself() {
        Permanent ranger = addCreatureReady(player1, new JuniperOrderRanger());

        harness.castFromHand(player1, new RonomUnicorn(), "{1}{W}");
        resolveAllTriggers();

        Permanent enteringCreature = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ranger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when Juniper Order Ranger itself enters")
    void doesNotTriggerOnItsOwnEntry() {
        harness.castFromHand(player1, new JuniperOrderRanger(), "{3}{G}{W}");
        resolveAllTriggers();

        Permanent ranger = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(ranger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new JuniperOrderRanger());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new RonomUnicorn(), "{1}{W}");
        resolveAllTriggers();

        Permanent enteringCreature = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers separately for each ally creature that enters")
    void triggersForEachAllyCreature() {
        Permanent ranger = addCreatureReady(player1, new JuniperOrderRanger());

        harness.castFromHand(player1, new RonomUnicorn(), "{1}{W}");
        resolveAllTriggers();
        Permanent firstEnteringCreature = gd.playerBattlefields.get(player1.getId()).getLast();

        harness.castFromHand(player1, new RonomUnicorn(), "{1}{W}");
        resolveAllTriggers();
        Permanent secondEnteringCreature = gd.playerBattlefields.get(player1.getId()).getLast();

        assertThat(firstEnteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondEnteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ranger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
