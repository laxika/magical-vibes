package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PyramidOfThePantheon;
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

@CardUsed({
        DocSamsonSuperPsychiatrist.class,
        GrizzlyBears.class,
        PyramidOfThePantheon.class,
        TimberlandGuide.class
})
class DocSamsonSuperPsychiatristTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one counter of each kind to counters put on a permanent you control")
    void addsOneCounterOfEachKindToControlledPermanents() {
        harness.addToBattlefield(player1, new DocSamsonSuperPsychiatrist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new PyramidOfThePantheon());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not add counters to permanents an opponent controls")
    void doesNotAddCountersToOpponentPermanents() {
        harness.addToBattlefield(player1, new DocSamsonSuperPsychiatrist());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, List.of(opponentBears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds mana equal to its current power in the chosen color")
    void addsManaEqualToCurrentPower() {
        Permanent doc = addCreatureReady(player1, new DocSamsonSuperPsychiatrist());
        doc.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(5);
    }
}
