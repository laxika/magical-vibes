package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pentavus;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BranchingEvolution.class, GrizzlyBears.class, Pentavus.class, TimberlandGuide.class})
class BranchingEvolutionTest extends BaseCardTest {

    @Test
    @DisplayName("doubles +1/+1 counters put on a creature you control")
    void doublesCountersOnControlledCreature() {
        harness.addToBattlefield(player1, new BranchingEvolution());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("does not double counters put on a creature an opponent controls")
    void doesNotDoubleOpponentCreatureCounters() {
        harness.addToBattlefield(player1, new BranchingEvolution());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("doubles +1/+1 counters a creature enters with")
    void doublesEnterWithCounters() {
        harness.addToBattlefield(player1, new BranchingEvolution());

        harness.setHand(player1, List.of(new Pentavus()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent pentavus = findPermanent(player1, "Pentavus");
        assertThat(pentavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
    }
}
