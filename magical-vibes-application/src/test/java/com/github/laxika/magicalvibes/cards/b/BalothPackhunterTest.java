package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.g.Gigantosaurus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalothPackhunter.class, Gigantosaurus.class, Cloudshift.class})
class BalothPackhunterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts two +1/+1 counters on each other same-name creature you control")
    void etbPutsCountersOnOtherControlledPackhunters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BalothPackhunter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BalothPackhunter());

        harness.castFromHand(player1, new BalothPackhunter(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Baloth Packhunter")).hasSize(3);
        assertThat(findPermanents(player1, "Baloth Packhunter").get(2)
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB does not affect another name or an opponent's creature")
    void etbOnlyAffectsOtherControlledPackhunters() {
        Permanent ownPackhunter = harness.addToBattlefieldAndReturn(player1, new BalothPackhunter());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new Gigantosaurus());
        Permanent opponentPackhunter = harness.addToBattlefieldAndReturn(player2, new BalothPackhunter());

        harness.castFromHand(player1, new BalothPackhunter(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownPackhunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentPackhunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A lone Packhunter receives no counters from its own trigger")
    void lonePackhunterDoesNotReceiveCounters() {
        harness.castFromHand(player1, new BalothPackhunter(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Baloth Packhunter")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The original trigger puts counters on its source after it leaves and returns")
    void originalTriggerAffectsReturnedPackhunter() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BalothPackhunter());
        harness.castFromHand(player1, new BalothPackhunter(), "{3}{G}");
        harness.passBothPriorities();
        Permanent entering = findPermanents(player1, "Baloth Packhunter").get(1);

        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, entering.getId());

        Permanent returned = findPermanents(player1, "Baloth Packhunter").get(1);
        assertThat(returned.getId()).isNotEqualTo(entering.getId());
        harness.passBothPriorities();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
