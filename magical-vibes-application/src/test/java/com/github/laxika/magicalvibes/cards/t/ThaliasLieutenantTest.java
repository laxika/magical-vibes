package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MildManneredLibrarian;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThaliasLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("Its entry puts a counter on each other Human you control, but not on itself")
    void entryCountersOtherHumans() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new MildManneredLibrarian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThaliasLieutenant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent lieutenant = findPermanent(player1, "Thalia's Lieutenant");
        assertThat(human.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Another Human entering under your control puts a counter on it")
    void anotherHumanEnteringCountersLieutenant() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new ThaliasLieutenant());
        harness.setHand(player1, List.of(new MildManneredLibrarian()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Human entering under an opponent's control does not trigger it")
    void opponentHumanEnteringDoesNotTrigger() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new ThaliasLieutenant());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new MildManneredLibrarian()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
