package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.ManifoldKey;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SquadCaptain.class, GreenwoodSentinel.class, ManifoldKey.class})
class SquadCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each other creature its controller controls")
    void entersWithCountersForOtherControlledCreatures() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        castSquadCaptain();

        Permanent captain = findPermanent(player1, "Squad Captain");
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get a counter when it is the only creature its controller controls")
    void doesNotCountItself() {
        castSquadCaptain();

        Permanent captain = findPermanent(player1, "Squad Captain");
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Noncreature permanents do not contribute entry counters")
    void doesNotCountNoncreaturePermanents() {
        harness.addToBattlefield(player1, new ManifoldKey());
        harness.addToBattlefield(player1, new GreenwoodSentinel());

        castSquadCaptain();

        assertThat(findPermanent(player1, "Squad Captain")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counts creatures at entry rather than when the spell is cast")
    void countsCreaturesAtEntry() {
        harness.castFromHand(player1, new SquadCaptain(), "{4}{W}");
        harness.enterBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Squad Captain")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Vigilance keeps Squad Captain untapped when attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent captain = addCreatureReady(player1, new SquadCaptain());

        declareAttackers(List.of(0));

        assertThat(captain.isTapped()).isFalse();
    }

    private void castSquadCaptain() {
        harness.castFromHand(player1, new SquadCaptain(), "{4}{W}");
        harness.passBothPriorities();
    }
}
