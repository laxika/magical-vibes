package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PyramidOfThePantheon;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DoublingSeasonTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles tokens created under its controller's control")
    void doublesTokens() {
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(2);
    }

    @Test
    @DisplayName("Doubles +1/+1 counters put on a permanent its controller controls")
    void doublesPlusOnePlusOneCounters() {
        harness.addToBattlefield(player1, new DoublingSeason());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Doubles noncreature counters put on a permanent its controller controls")
    void doublesNoncreatureCounters() {
        harness.addToBattlefield(player1, new DoublingSeason());
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new PyramidOfThePantheon());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isEqualTo(2);
    }
}
