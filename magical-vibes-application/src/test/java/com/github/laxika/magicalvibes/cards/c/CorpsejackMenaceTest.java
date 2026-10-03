package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pentavus;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpsejackMenace.class, GrizzlyBears.class, Pentavus.class, TimberlandGuide.class,
        CommonBond.class, TurnToFrog.class})
class CorpsejackMenaceTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles +1/+1 counters put on a creature you control")
    void doublesCountersOnControlledCreature() {
        harness.addToBattlefield(player1, new CorpsejackMenace());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Timberland Guide would put one +1/+1 counter; Corpsejack doubles to two.
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not double +1/+1 counters on a creature an opponent controls")
    void doesNotDoubleOnOpponentCreature() {
        harness.addToBattlefield(player1, new CorpsejackMenace());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Corpsejacks stack, multiplying +1/+1 counters by four")
    void twoCorpsejacksStack() {
        harness.addToBattlefield(player1, new CorpsejackMenace());
        harness.addToBattlefield(player1, new CorpsejackMenace());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Doubles +1/+1 counters a creature enters with")
    void doublesEnterWithCounters() {
        harness.addToBattlefield(player1, new CorpsejackMenace());

        harness.castFromHand(player1, new Pentavus(), "{7}");
        harness.passBothPriorities();

        Permanent pentavus = findPermanent(player1, "Pentavus");
        // Pentavus enters with five; Corpsejack doubles to ten.
        assertThat(pentavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
    }

    @Test
    @DisplayName("Doubles counters put on Corpsejack Menace itself")
    void doublesCountersOnItself() {
        Permanent menace = harness.addToBattlefieldAndReturn(player1, new CorpsejackMenace());
        harness.setHand(player1, List.of(new CommonBond()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(menace.getId()));
        harness.passBothPriorities();

        assertThat(menace.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Doubles counters placed by an opponent on a creature you control")
    void doublesCountersRegardlessOfSpellController() {
        Permanent menace = harness.addToBattlefieldAndReturn(player1, new CorpsejackMenace());
        harness.setHand(player2, List.of(new CommonBond()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, List.of(menace.getId()));
        harness.passBothPriorities();

        assertThat(menace.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not double counters after losing all abilities")
    void stopsDoublingWhenAbilitiesAreRemoved() {
        Permanent menace = harness.addToBattlefieldAndReturn(player1, new CorpsejackMenace());
        harness.setHand(player1, List.of(new TurnToFrog(), new CommonBond()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, menace.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, List.of(menace.getId()));
        harness.passBothPriorities();

        assertThat(menace.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
