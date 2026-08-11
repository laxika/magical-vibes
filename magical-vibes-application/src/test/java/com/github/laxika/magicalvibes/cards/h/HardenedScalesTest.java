package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pentavus;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HardenedScalesTest extends BaseCardTest {

    @Test
    @DisplayName("adds one +1/+1 counter to counters put on a creature you control")
    void addsOneCounterOnControlledCreature() {
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("does not add a counter to a creature an opponent controls")
    void doesNotAddOnOpponentCreature() {
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("multiple Hardened Scales add one counter each")
    void multipleScalesStack() {
        harness.addToBattlefield(player1, new HardenedScales());
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("adds one counter to a creature entering with +1/+1 counters")
    void addsOneToEnterWithCounters() {
        harness.addToBattlefield(player1, new HardenedScales());

        harness.setHand(player1, List.of(new Pentavus()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent pentavus = findPermanent(player1, "Pentavus");
        assertThat(pentavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }
}
