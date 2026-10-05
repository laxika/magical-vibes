package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ChillToTheBone;
import com.github.laxika.magicalvibes.cards.r.RonomUnicorn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JuniperOrderRanger.class, RonomUnicorn.class, ChillToTheBone.class})
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

    @Test
    @DisplayName("An existing Ranger puts a counter on a second Ranger entering")
    void triggersForAnotherRanger() {
        Permanent firstRanger = addCreatureReady(player1, new JuniperOrderRanger());

        harness.castFromHand(player1, new JuniperOrderRanger(), "{3}{G}{W}");
        resolveAllTriggers();

        Permanent secondRanger = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(firstRanger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondRanger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castFromHand(player1, new RonomUnicorn(), "{1}{W}");
        resolveAllTriggers();

        Permanent unicorn = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(unicorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(firstRanger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondRanger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Still puts a counter on the Ranger when the entering creature dies in response")
    void stillCountersRangerWhenEnteringCreatureDies() {
        Permanent ranger = addCreatureReady(player1, new JuniperOrderRanger());
        harness.setHand(player2, List.of(new ChillToTheBone()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castFromHand(player1, new RonomUnicorn(), "{1}{W}");
        harness.passBothPriorities();
        Permanent unicorn = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gd.stack).hasSize(1);
        assertThat(ranger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(unicorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player2, 0, unicorn.getId());
        harness.assertInGraveyard(player1, "Ronom Unicorn");
        resolveAllTriggers();

        assertThat(ranger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(unicorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Still puts a counter on the entering creature when the Ranger dies in response")
    void stillCountersEnteringCreatureWhenRangerDies() {
        Permanent ranger = addCreatureReady(player1, new JuniperOrderRanger());
        harness.setHand(player2, List.of(new ChillToTheBone()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castFromHand(player1, new RonomUnicorn(), "{1}{W}");
        harness.passBothPriorities();
        Permanent unicorn = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player2, 0, ranger.getId());
        harness.assertInGraveyard(player1, "Juniper Order Ranger");
        resolveAllTriggers();

        assertThat(unicorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ranger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
